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

/** 热门投资方向实时收益任务。 */
@Service
@RequiredArgsConstructor
public class HotThemeReturnTaskHandler implements InvestmentTaskHandler {
    private final MarketQuoteStore quotes;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 支持热门主题收益任务。 */
    @Override
    public boolean supports(String taskType) {
        return "HOT_THEME_RETURN".equals(taskType);
    }

    /** 计算每个配置主题在回看窗口内的平均收益并保存快照。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        int windowMinutes = TaskParameterParser.positiveInt(
            event.parameters(), "windowMinutes", 1440);
        String marketScope = TaskParameterParser.marketScope(event.parameters());
        LocalDateTime now = clock.now();
        Map<String, List<String>> themes = TaskParameterParser.themes(event.parameters());
        themes.forEach((themeName, productCodes) ->
            saveSnapshot(event.taskCode(), "RETURN", themeName, windowMinutes,
                marketScope, quotes.findPerformance(productCodes, now.minusMinutes(windowMinutes), now), now));
        return "已生成 " + themes.size() + " 个热门投资方向收益快照";
    }

    /** 保存单个主题收益快照。 */
    private void saveSnapshot(
        String taskCode,
        String snapshotType,
        String themeName,
        int windowMinutes,
        String marketScope,
        List<ThemeProductPerformance> performances,
        LocalDateTime now
    ) {
        List<BigDecimal> returns = performances.stream()
            .map(ThemeProductPerformance::returnRate)
            .filter(value -> value != null)
            .toList();
        BigDecimal average = average(returns);
        ThemeProductPerformance top = performances.stream()
            .filter(item -> item.returnRate() != null)
            .max(Comparator.comparing(ThemeProductPerformance::returnRate))
            .orElse(null);
        ReturnQualityMetrics qualityMetrics = calculateQualityMetrics(performances, returns, top);
        snapshots.save(InvestmentThemeSnapshot.builder()
            .bizId(ids.newBizId())
            .taskCode(taskCode)
            .snapshotType(snapshotType)
            .themeCode(TaskParameterParser.themeCode(themeName))
            .themeName(themeName)
            .marketScope(marketScope)
            .windowMinutes(windowMinutes)
            .sampleCount(returns.size())
            .returnRate(average)
            .topProductBizId(top == null ? null : top.productBizId())
            .metrics(writeMetrics(performances, qualityMetrics))
            .snapshotTime(now)
            .createdAt(now)
            .build());
    }

    /**
     * 计算主题收益快照质量指标。
     *
     * @param performances 原始产品样本
     * @param returns 有效收益率集合
     * @param top 收益最高产品
     * @return 主题收益质量指标
     * @author dz
     * @date 2026-06-21
     */
    private ReturnQualityMetrics calculateQualityMetrics(
        List<ThemeProductPerformance> performances,
        List<BigDecimal> returns,
        ThemeProductPerformance top
    ) {
        BigDecimal coverageRate = performances.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(returns.size())
                .divide(BigDecimal.valueOf(performances.size()), 4, RoundingMode.HALF_UP);
        BigDecimal volatility = volatility(returns);
        BigDecimal topContribution = top == null || returns.isEmpty()
            ? BigDecimal.ZERO
            : top.returnRate().abs()
                .divide(sumAbs(returns), 4, RoundingMode.HALF_UP);
        BigDecimal qualityScore = coverageRate.multiply(BigDecimal.valueOf(0.60))
            .add(sampleScore(returns.size()).multiply(BigDecimal.valueOf(0.25)))
            .add(volatilityPenalty(volatility).multiply(BigDecimal.valueOf(0.15)))
            .setScale(4, RoundingMode.HALF_UP);
        return ReturnQualityMetrics.builder()
            .requestedProductCount(performances.size())
            .validReturnCount(returns.size())
            .coverageRate(coverageRate)
            .volatility(volatility)
            .topContribution(topContribution)
            .qualityScore(qualityScore)
            .qualityLevel(resolveQualityLevel(qualityScore))
            .build();
    }

    /**
     * 将可解释样本明细序列化为 JSON。
     *
     * @param performances 产品收益样本
     * @param qualityMetrics 统计质量指标
     * @return 可解释指标 JSON
     * @author dz
     * @date 2026-06-21
     */
    private String writeMetrics(
        List<ThemeProductPerformance> performances,
        ReturnQualityMetrics qualityMetrics
    ) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("requestedProductCount", qualityMetrics.requestedProductCount());
            payload.put("validReturnCount", qualityMetrics.validReturnCount());
            payload.put("coverageRate", qualityMetrics.coverageRate());
            payload.put("volatility", qualityMetrics.volatility());
            payload.put("topContribution", qualityMetrics.topContribution());
            payload.put("qualityScore", qualityMetrics.qualityScore());
            payload.put("qualityLevel", qualityMetrics.qualityLevel());
            payload.put("performances", performances);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("主题收益指标序列化失败", exception);
        }
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
     * 计算绝对收益总和，避免除零。
     *
     * @param returns 有效收益率集合
     * @return 绝对收益总和
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal sumAbs(List<BigDecimal> returns) {
        BigDecimal sum = returns.stream()
            .map(BigDecimal::abs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : sum;
    }

    /**
     * 根据样本数计算质量分。
     *
     * @param sampleCount 有效样本数
     * @return 样本质量分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal sampleScore(int sampleCount) {
        return BigDecimal.valueOf(Math.min(sampleCount, 5))
            .divide(BigDecimal.valueOf(5), 4, RoundingMode.HALF_UP);
    }

    /**
     * 根据波动计算质量扣减后的稳定性分。
     *
     * @param volatility 平均绝对偏离
     * @return 稳定性分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal volatilityPenalty(BigDecimal volatility) {
        BigDecimal penalty = volatility.min(BigDecimal.valueOf(0.20));
        return BigDecimal.ONE.subtract(penalty.multiply(BigDecimal.valueOf(2)))
            .max(BigDecimal.ZERO)
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

    /** 主题收益快照质量指标。 */
    @lombok.Builder
    private record ReturnQualityMetrics(
        int requestedProductCount,
        int validReturnCount,
        BigDecimal coverageRate,
        BigDecimal volatility,
        BigDecimal topContribution,
        BigDecimal qualityScore,
        String qualityLevel
    ) {
    }
}
