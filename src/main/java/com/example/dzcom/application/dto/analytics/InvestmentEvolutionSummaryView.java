package com.example.dzcom.application.dto.analytics;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 投资闭环持续进化分析视图。
 *
 * <p>该视图汇总最近闭环、报告、Mock 组合、风控、反馈、回测和模型调用审计样本，
 * 用于回答当前自动投资链路是否具备持续进化和可信任扩展的证据基础。</p>
 */
@Builder
public record InvestmentEvolutionSummaryView(
    LocalDateTime generatedAt,
    String sampleStatus,
    int sampleWindowSize,
    List<MetricView> kpis,
    ClosedLoopMetricsView closedLoop,
    PortfolioMetricsView portfolio,
    RiskMetricsView risk,
    ModelMetricsView model,
    FeedbackMetricsView feedback,
    List<VariantPerformanceView> variants,
    List<RecentRunView> recentRuns,
    List<String> limitations
) {
    /**
     * 分析页通用指标卡。
     *
     * @param code 指标编码
     * @param label 展示名称
     * @param value 展示值
     * @param status 指标状态：GOOD/WARN/BAD/UNKNOWN
     * @param hint 指标解释
     */
    @Builder
    public record MetricView(String code, String label, String value, String status, String hint) {
    }

    /**
     * 自动闭环运行指标。
     *
     * @param sampleCount 闭环样本数
     * @param successCount 成功数量
     * @param failedCount 失败数量
     * @param runningCount 运行中数量
     * @param blockedCount 阻断数量
     * @param successRate 成功率
     * @param averageQualityScore 平均报告质量分
     * @param gatePassCount 质量门禁通过数量
     */
    @Builder
    public record ClosedLoopMetricsView(
        int sampleCount,
        long successCount,
        long failedCount,
        long runningCount,
        long blockedCount,
        BigDecimal successRate,
        BigDecimal averageQualityScore,
        long gatePassCount
    ) {
    }

    /**
     * Mock 组合与交易指标。
     *
     * @param portfolioCount 纳入统计的组合数量
     * @param valuationPointCount 估值点数量
     * @param latestReturnRate 最新累计收益率
     * @param maxDrawdown 最大回撤
     * @param orderEventCount 最近订单事件数量
     * @param filledOrderEventCount 最近成交事件数量
     * @param turnoverProxy 换手率代理指标，样本不足时仅作方向参考
     */
    @Builder
    public record PortfolioMetricsView(
        int portfolioCount,
        int valuationPointCount,
        BigDecimal latestReturnRate,
        BigDecimal maxDrawdown,
        int orderEventCount,
        int filledOrderEventCount,
        BigDecimal turnoverProxy
    ) {
    }

    /**
     * 风控审计指标。
     *
     * @param sampleCount 风控样本数
     * @param passCount 通过数量
     * @param reviewCount 复核数量
     * @param rejectCount 拒绝数量
     * @param topRejectReasons 高频拒绝原因
     */
    @Builder
    public record RiskMetricsView(
        int sampleCount,
        long passCount,
        long reviewCount,
        long rejectCount,
        List<ReasonCountView> topRejectReasons
    ) {
    }

    /**
     * 模型调用稳定性指标。
     *
     * @param sampleCount 模型调用样本数
     * @param successCount 成功数量
     * @param failedCount 失败数量
     * @param successRate 成功率
     * @param averageDurationMs 平均耗时毫秒
     */
    @Builder
    public record ModelMetricsView(
        int sampleCount,
        long successCount,
        long failedCount,
        BigDecimal successRate,
        BigDecimal averageDurationMs
    ) {
    }

    /**
     * 反馈和回测样本指标。
     *
     * @param feedbackCount 反馈样本数
     * @param positiveFeedbackCount 正向反馈数量
     * @param negativeFeedbackCount 负向反馈数量
     * @param backtestCount 回测样本数
     * @param completedBacktestCount 完成回测数量
     */
    @Builder
    public record FeedbackMetricsView(
        int feedbackCount,
        long positiveFeedbackCount,
        long negativeFeedbackCount,
        int backtestCount,
        long completedBacktestCount
    ) {
    }

    /**
     * A/B 归因变体表现。
     *
     * @param variantKey 模型、Prompt、Skill、场景组合成的归因键
     * @param modelDisplay 模型展示
     * @param promptDisplay Prompt 展示
     * @param skillDisplay Skill 展示
     * @param sampleCount 样本数量
     * @param successRate 调用成功率
     * @param averageDurationMs 平均耗时毫秒
     * @param latestBusinessLabel 最近关联业务
     */
    @Builder
    public record VariantPerformanceView(
        String variantKey,
        String modelDisplay,
        String promptDisplay,
        String skillDisplay,
        int sampleCount,
        BigDecimal successRate,
        BigDecimal averageDurationMs,
        String latestBusinessLabel
    ) {
    }

    /**
     * 最近闭环运行摘要。
     *
     * @param runBizId 运行标识
     * @param runNo 运行编号
     * @param runStatus 运行状态
     * @param reportBizId 报告标识
     * @param portfolioBizId Mock 组合标识
     * @param promptDisplay Prompt 展示
     * @param qualityScore 质量分
     * @param gateResult 门禁结果
     * @param startedAt 开始时间
     */
    @Builder
    public record RecentRunView(
        String runBizId,
        String runNo,
        String runStatus,
        String reportBizId,
        String portfolioBizId,
        String promptDisplay,
        BigDecimal qualityScore,
        String gateResult,
        LocalDateTime startedAt
    ) {
    }

    /**
     * 原因分布项。
     *
     * @param reasonCode 原因编码
     * @param count 数量
     */
    @Builder
    public record ReasonCountView(String reasonCode, long count) {
    }
}
