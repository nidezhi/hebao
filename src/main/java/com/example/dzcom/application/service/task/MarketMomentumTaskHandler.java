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
import java.util.LinkedHashMap;
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
        BigDecimal average = average(returns);
        long positiveCount = returns.stream().filter(value -> value.signum() > 0).count();
        BigDecimal breadth = returns.isEmpty() ? BigDecimal.ZERO
            : BigDecimal.valueOf(positiveCount)
                .divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        BigDecimal volatility = volatility(returns);
        BigDecimal qualityScore = qualityScore(performances.size(), returns.size(), volatility);
        BigDecimal momentum = average.multiply(breadth)
            .multiply(qualityScore)
            .setScale(8, RoundingMode.HALF_UP);
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
            .metrics(writeMetrics(buildMetrics(
                performances,
                returns,
                positiveCount,
                breadth,
                volatility,
                qualityScore
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

    /**
     * 构建可解释动量指标。
     *
     * @param performances 原始产品样本
     * @param returns 有效收益率集合
     * @param positiveCount 正收益样本数
     * @param breadth 上涨广度
     * @param volatility 收益波动
     * @param qualityScore 质量分
     * @return 有序指标集合
     * @author dz
     * @date 2026-06-21
     */
    private Map<String, Object> buildMetrics(
        List<ThemeProductPerformance> performances,
        List<BigDecimal> returns,
        long positiveCount,
        BigDecimal breadth,
        BigDecimal volatility,
        BigDecimal qualityScore
    ) {
        BigDecimal coverageRate = performances.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(returns.size())
                .divide(BigDecimal.valueOf(performances.size()), 4, RoundingMode.HALF_UP);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("requestedProductCount", performances.size());
        metrics.put("validReturnCount", returns.size());
        metrics.put("positiveCount", positiveCount);
        metrics.put("positiveBreadth", breadth);
        metrics.put("coverageRate", coverageRate);
        metrics.put("volatility", volatility);
        metrics.put("qualityScore", qualityScore);
        metrics.put("qualityLevel", resolveQualityLevel(qualityScore));
        metrics.put("performances", performances);
        return metrics;
    }

    /**
     * 计算收益率均值。
     *
     * @param returns 有效收益率集合
     * @return 平均收益率
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal average(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = returns.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
    }

    /**
     * 计算收益率波动。
     *
     * @param returns 有效收益率集合
     * @return 平均绝对偏离
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal volatility(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal average = average(returns);
        BigDecimal deviation = returns.stream()
            .map(value -> value.subtract(average).abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return deviation.divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
    }

    /**
     * 计算动量统计质量分。
     *
     * @param requestedCount 配置产品数
     * @param validCount 有效收益样本数
     * @param volatility 收益波动
     * @return 质量分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal qualityScore(int requestedCount, int validCount, BigDecimal volatility) {
        BigDecimal coverageRate = requestedCount == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(validCount)
                .divide(BigDecimal.valueOf(requestedCount), 4, RoundingMode.HALF_UP);
        BigDecimal sampleScore = BigDecimal.valueOf(Math.min(validCount, 5))
            .divide(BigDecimal.valueOf(5), 4, RoundingMode.HALF_UP);
        BigDecimal stabilityScore = BigDecimal.ONE.subtract(
                volatility.min(BigDecimal.valueOf(0.20)).multiply(BigDecimal.valueOf(2)))
            .max(BigDecimal.ZERO)
            .setScale(4, RoundingMode.HALF_UP);
        return coverageRate.multiply(BigDecimal.valueOf(0.55))
            .add(sampleScore.multiply(BigDecimal.valueOf(0.25)))
            .add(stabilityScore.multiply(BigDecimal.valueOf(0.20)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 将质量分转换为等级。
     *
     * @param qualityScore 质量分
     * @return HIGH/MEDIUM/LOW
     * @author dz
     * @date 2026-06-21
     */
    private String resolveQualityLevel(BigDecimal qualityScore) {
        if (qualityScore.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
            return "HIGH";
        }
        if (qualityScore.compareTo(BigDecimal.valueOf(0.45)) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
