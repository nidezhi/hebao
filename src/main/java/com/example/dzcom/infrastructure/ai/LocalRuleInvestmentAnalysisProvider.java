package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.application.service.task.TaskParameterParser;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 基于已入库快照的本地规则投资分析提供方。 */
@Component
@RequiredArgsConstructor
public class LocalRuleInvestmentAnalysisProvider implements InvestmentAnalysisProvider {
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 支持默认本地规则分析提供方。 */
    @Override
    public boolean supports(String providerCode) {
        return "LOCAL_RULE".equals(providerCode);
    }

    /** 生成投资信息汇总、趋势、投资方案、模拟收益和图表 payload。 */
    @Override
    public InvestmentAnalysisReport analyze(String requestId, GenerateInvestmentAnalysisCommand command) {
        LocalDateTime now = clock.now();
        String marketScope = command.marketScope() == null || command.marketScope().isBlank()
            ? TaskParameterParser.CN_MAINLAND
            : command.marketScope();
        int lookbackDays = command.lookbackDays() == null || command.lookbackDays() < 1
            ? 30
            : command.lookbackDays();
        List<InvestmentThemeSnapshot> samples = snapshots.search(new InvestmentThemeSnapshotSearchCriteria(
            null,
            null,
            command.themeCode(),
            marketScope,
            now.minusDays(lookbackDays),
            now,
            1,
            100,
            "snapshotTime",
            false
        )).items();
        InvestmentThemeSnapshot latest = samples.stream()
            .max(Comparator.comparing(InvestmentThemeSnapshot::snapshotTime))
            .orElse(null);
        BigDecimal averageReturn = average(samples.stream()
            .map(InvestmentThemeSnapshot::returnRate)
            .filter(value -> value != null)
            .toList());
        BigDecimal initialCapital = command.initialCapital() == null
            ? BigDecimal.valueOf(100000)
            : command.initialCapital();
        BigDecimal estimatedProfit = initialCapital.multiply(averageReturn)
            .setScale(4, RoundingMode.HALF_UP);
        return InvestmentAnalysisReport.builder()
            .bizId(ids.newBizId())
            .requestId(requestId)
            .providerCode("LOCAL_RULE")
            .modelCode(command.modelCode() == null || command.modelCode().isBlank()
                ? "local-rule-v1"
                : command.modelCode())
            .marketScope(marketScope)
            .themeCode(command.themeCode())
            .themeName(latest == null ? null : latest.themeName())
            .status("SUCCEEDED")
            .investmentSummary(writeJson(Map.of(
                "marketScope", marketScope,
                "themeCode", command.themeCode(),
                "sampleCount", samples.size(),
                "averageReturn", averageReturn,
                "latestSnapshotTime", latest == null ? "" : latest.snapshotTime()
            )))
            .trend(writeJson(Map.of(
                "direction", averageReturn.signum() >= 0 ? "UP" : "DOWN",
                "averageReturn", averageReturn,
                "lookbackDays", lookbackDays
            )))
            .investmentPlan(writeJson(Map.of(
                "planType", "REFERENCE_ALLOCATION",
                "riskNotice", "AI 分析仅为投资辅助信息，不构成收益承诺或自动交易指令。",
                "suggestedAction", averageReturn.signum() >= 0 ? "关注并分批配置" : "降低仓位并等待确认"
            )))
            .simulatedReturn(writeJson(Map.of(
                "initialCapital", initialCapital,
                "estimatedProfit", estimatedProfit,
                "estimatedFinalCapital", initialCapital.add(estimatedProfit),
                "returnRate", averageReturn
            )))
            .chartPayload(writeJson(Map.of(
                "series", samples.stream()
                    .sorted(Comparator.comparing(InvestmentThemeSnapshot::snapshotTime))
                    .map(sample -> Map.of(
                        "time", sample.snapshotTime(),
                        "snapshotType", sample.snapshotType(),
                        "returnRate", sample.returnRate() == null ? BigDecimal.ZERO : sample.returnRate(),
                        "momentumScore", sample.momentumScore() == null ? BigDecimal.ZERO : sample.momentumScore(),
                        "heatScore", sample.heatScore() == null ? BigDecimal.ZERO : sample.heatScore()
                    ))
                    .toList()
            )))
            .promptSnapshot(writeJson(Map.of(
                "providerCode", "LOCAL_RULE",
                "marketScope", marketScope,
                "themeCode", command.themeCode() == null ? "" : command.themeCode(),
                "lookbackDays", lookbackDays
            )))
            .generatedAt(now)
            .createdAt(now)
            .build();
    }

    /** 计算均值，空集合返回 0。 */
    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return values.isEmpty()
            ? BigDecimal.ZERO
            : sum.divide(BigDecimal.valueOf(values.size()), 8, RoundingMode.HALF_UP);
    }

    /** 序列化结构化分析片段。 */
    private String writeJson(Map<String, ?> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("投资分析结果序列化失败", exception);
        }
    }
}
