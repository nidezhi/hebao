package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.ThemeProductPerformance;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 市场主题动量扫描任务。 */
@Service
@RequiredArgsConstructor
public class MarketMomentumTaskHandler implements InvestmentTaskHandler {
    private final MarketQuoteStore quotes;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 支持市场动量扫描任务。 */
    @Override
    public boolean supports(String taskType) {
        return "MARKET_MOMENTUM_SCAN".equals(taskType);
    }

    /** 根据收益方向一致性和平均收益计算主题动量评分。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        int windowMinutes = TaskParameterParser.positiveInt(
            event.parameters(), "windowMinutes", 60);
        String marketScope = TaskParameterParser.marketScope(event.parameters());
        LocalDateTime now = clock.now();
        Map<String, List<String>> themes = TaskParameterParser.themes(event.parameters());
        themes.forEach((themeName, productCodes) -> {
            List<ThemeProductPerformance> performances = quotes.findPerformance(
                productCodes, now.minusMinutes(windowMinutes), now);
            snapshots.save(buildSnapshot(
                event.taskCode(), themeName, marketScope, windowMinutes, performances, now));
        });
        return "已完成 " + themes.size() + " 个投资方向的市场动量扫描";
    }

    /** 构建可解释的主题动量快照。 */
    private InvestmentThemeSnapshot buildSnapshot(
        String taskCode,
        String themeName,
        String marketScope,
        int windowMinutes,
        List<ThemeProductPerformance> performances,
        LocalDateTime now
    ) {
        List<BigDecimal> returns = performances.stream()
            .map(ThemeProductPerformance::returnRate)
            .filter(value -> value != null)
            .toList();
        BigDecimal average = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!returns.isEmpty()) {
            average = average.divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        }
        long positiveCount = returns.stream().filter(value -> value.signum() > 0).count();
        BigDecimal breadth = returns.isEmpty() ? BigDecimal.ZERO
            : BigDecimal.valueOf(positiveCount)
                .divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        BigDecimal momentum = average.multiply(breadth).setScale(8, RoundingMode.HALF_UP);
        ThemeProductPerformance top = performances.stream()
            .filter(item -> item.returnRate() != null)
            .max(Comparator.comparing(ThemeProductPerformance::returnRate))
            .orElse(null);
        return InvestmentThemeSnapshot.builder()
            .bizId(ids.newBizId())
            .taskCode(taskCode)
            .snapshotType("MOMENTUM")
            .themeCode(TaskParameterParser.themeCode(themeName))
            .themeName(themeName)
            .marketScope(marketScope)
            .windowMinutes(windowMinutes)
            .sampleCount(returns.size())
            .returnRate(average)
            .momentumScore(momentum)
            .topProductBizId(top == null ? null : top.productBizId())
            .metrics(writeMetrics(Map.of(
                "positiveBreadth", breadth,
                "performances", performances
            )))
            .snapshotTime(now)
            .createdAt(now)
            .build();
    }

    /** 序列化动量指标。 */
    private String writeMetrics(Map<String, Object> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("市场动量指标序列化失败", exception);
        }
    }
}
