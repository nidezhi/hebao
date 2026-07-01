package com.example.dzcom.interfaces.dto.response.analytics;

import com.example.dzcom.application.dto.analytics.InvestmentEvolutionSummaryView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 投资闭环持续进化分析响应，面向前端统一分析页提供结构化指标和样本限制。 */
@Builder
@Schema(description = "投资闭环持续进化分析响应")
public record InvestmentEvolutionSummaryResponse(
    @Schema(description = "分析生成时间")
    LocalDateTime generatedAt,
    @Schema(description = "样本状态：INSUFFICIENT_SAMPLE/EARLY_SIGNAL/ENOUGH_FOR_TREND")
    String sampleStatus,
    @Schema(description = "样本窗口大小")
    int sampleWindowSize,
    @Schema(description = "顶部指标卡")
    List<MetricResponse> kpis,
    @Schema(description = "自动闭环运行指标")
    ClosedLoopMetricsResponse closedLoop,
    @Schema(description = "Mock 组合收益与交易指标")
    PortfolioMetricsResponse portfolio,
    @Schema(description = "风控审计指标")
    RiskMetricsResponse risk,
    @Schema(description = "模型调用稳定性指标")
    ModelMetricsResponse model,
    @Schema(description = "反馈和回测指标")
    FeedbackMetricsResponse feedback,
    @Schema(description = "模型/Prompt/Skill A/B 归因表现")
    List<VariantPerformanceResponse> variants,
    @Schema(description = "最近闭环运行摘要")
    List<RecentRunResponse> recentRuns,
    @Schema(description = "当前样本和口径限制")
    List<String> limitations
) {
    /**
     * 从应用层视图转换为接口响应。
     *
     * @param view 应用层持续进化分析视图
     * @return 接口响应 DTO
     */
    public static InvestmentEvolutionSummaryResponse from(InvestmentEvolutionSummaryView view) {
        return InvestmentEvolutionSummaryResponse.builder()
            .generatedAt(view.generatedAt())
            .sampleStatus(view.sampleStatus())
            .sampleWindowSize(view.sampleWindowSize())
            .kpis(view.kpis().stream().map(MetricResponse::from).toList())
            .closedLoop(ClosedLoopMetricsResponse.from(view.closedLoop()))
            .portfolio(PortfolioMetricsResponse.from(view.portfolio()))
            .risk(RiskMetricsResponse.from(view.risk()))
            .model(ModelMetricsResponse.from(view.model()))
            .feedback(FeedbackMetricsResponse.from(view.feedback()))
            .variants(view.variants().stream().map(VariantPerformanceResponse::from).toList())
            .recentRuns(view.recentRuns().stream().map(RecentRunResponse::from).toList())
            .limitations(view.limitations())
            .build();
    }

    /** 分析页通用指标卡响应。 */
    @Builder
    @Schema(description = "分析页通用指标卡响应")
    public record MetricResponse(
        @Schema(description = "指标编码") String code,
        @Schema(description = "指标名称") String label,
        @Schema(description = "展示值") String value,
        @Schema(description = "状态：GOOD/WARN/BAD/UNKNOWN") String status,
        @Schema(description = "指标说明") String hint
    ) {
        /**
         * 从应用层指标转换为响应。
         *
         * @param view 应用层指标视图
         * @return 指标响应
         */
        public static MetricResponse from(InvestmentEvolutionSummaryView.MetricView view) {
            return MetricResponse.builder()
                .code(view.code())
                .label(view.label())
                .value(view.value())
                .status(view.status())
                .hint(view.hint())
                .build();
        }
    }

    /** 自动闭环运行指标响应。 */
    @Builder
    @Schema(description = "自动闭环运行指标响应")
    public record ClosedLoopMetricsResponse(
        @Schema(description = "闭环样本数") int sampleCount,
        @Schema(description = "成功数量") long successCount,
        @Schema(description = "失败数量") long failedCount,
        @Schema(description = "运行中数量") long runningCount,
        @Schema(description = "阻断数量") long blockedCount,
        @Schema(description = "成功率") BigDecimal successRate,
        @Schema(description = "平均报告质量分") BigDecimal averageQualityScore,
        @Schema(description = "质量门禁通过数量") long gatePassCount
    ) {
        /**
         * 从应用层闭环指标转换为响应。
         *
         * @param view 应用层闭环指标
         * @return 闭环指标响应
         */
        public static ClosedLoopMetricsResponse from(InvestmentEvolutionSummaryView.ClosedLoopMetricsView view) {
            return ClosedLoopMetricsResponse.builder()
                .sampleCount(view.sampleCount())
                .successCount(view.successCount())
                .failedCount(view.failedCount())
                .runningCount(view.runningCount())
                .blockedCount(view.blockedCount())
                .successRate(view.successRate())
                .averageQualityScore(view.averageQualityScore())
                .gatePassCount(view.gatePassCount())
                .build();
        }
    }

    /** Mock 组合收益与交易指标响应。 */
    @Builder
    @Schema(description = "Mock 组合收益与交易指标响应")
    public record PortfolioMetricsResponse(
        @Schema(description = "组合数量") int portfolioCount,
        @Schema(description = "估值点数量") int valuationPointCount,
        @Schema(description = "最新累计收益率") BigDecimal latestReturnRate,
        @Schema(description = "最大回撤") BigDecimal maxDrawdown,
        @Schema(description = "最近订单事件数量") int orderEventCount,
        @Schema(description = "最近成交事件数量") int filledOrderEventCount,
        @Schema(description = "换手率代理指标") BigDecimal turnoverProxy
    ) {
        /**
         * 从应用层组合指标转换为响应。
         *
         * @param view 应用层组合指标
         * @return 组合指标响应
         */
        public static PortfolioMetricsResponse from(InvestmentEvolutionSummaryView.PortfolioMetricsView view) {
            return PortfolioMetricsResponse.builder()
                .portfolioCount(view.portfolioCount())
                .valuationPointCount(view.valuationPointCount())
                .latestReturnRate(view.latestReturnRate())
                .maxDrawdown(view.maxDrawdown())
                .orderEventCount(view.orderEventCount())
                .filledOrderEventCount(view.filledOrderEventCount())
                .turnoverProxy(view.turnoverProxy())
                .build();
        }
    }

    /** 风控审计指标响应。 */
    @Builder
    @Schema(description = "风控审计指标响应")
    public record RiskMetricsResponse(
        @Schema(description = "风控样本数") int sampleCount,
        @Schema(description = "通过数量") long passCount,
        @Schema(description = "复核数量") long reviewCount,
        @Schema(description = "拒绝数量") long rejectCount,
        @Schema(description = "高频拒绝原因") List<ReasonCountResponse> topRejectReasons
    ) {
        /**
         * 从应用层风控指标转换为响应。
         *
         * @param view 应用层风控指标
         * @return 风控指标响应
         */
        public static RiskMetricsResponse from(InvestmentEvolutionSummaryView.RiskMetricsView view) {
            return RiskMetricsResponse.builder()
                .sampleCount(view.sampleCount())
                .passCount(view.passCount())
                .reviewCount(view.reviewCount())
                .rejectCount(view.rejectCount())
                .topRejectReasons(view.topRejectReasons().stream().map(ReasonCountResponse::from).toList())
                .build();
        }
    }

    /** 模型调用稳定性指标响应。 */
    @Builder
    @Schema(description = "模型调用稳定性指标响应")
    public record ModelMetricsResponse(
        @Schema(description = "模型调用样本数") int sampleCount,
        @Schema(description = "成功数量") long successCount,
        @Schema(description = "失败数量") long failedCount,
        @Schema(description = "成功率") BigDecimal successRate,
        @Schema(description = "平均耗时毫秒") BigDecimal averageDurationMs
    ) {
        /**
         * 从应用层模型指标转换为响应。
         *
         * @param view 应用层模型指标
         * @return 模型指标响应
         */
        public static ModelMetricsResponse from(InvestmentEvolutionSummaryView.ModelMetricsView view) {
            return ModelMetricsResponse.builder()
                .sampleCount(view.sampleCount())
                .successCount(view.successCount())
                .failedCount(view.failedCount())
                .successRate(view.successRate())
                .averageDurationMs(view.averageDurationMs())
                .build();
        }
    }

    /** 反馈和回测指标响应。 */
    @Builder
    @Schema(description = "反馈和回测指标响应")
    public record FeedbackMetricsResponse(
        @Schema(description = "反馈样本数") int feedbackCount,
        @Schema(description = "正向反馈数量") long positiveFeedbackCount,
        @Schema(description = "负向反馈数量") long negativeFeedbackCount,
        @Schema(description = "回测样本数") int backtestCount,
        @Schema(description = "完成回测数量") long completedBacktestCount
    ) {
        /**
         * 从应用层反馈指标转换为响应。
         *
         * @param view 应用层反馈指标
         * @return 反馈指标响应
         */
        public static FeedbackMetricsResponse from(InvestmentEvolutionSummaryView.FeedbackMetricsView view) {
            return FeedbackMetricsResponse.builder()
                .feedbackCount(view.feedbackCount())
                .positiveFeedbackCount(view.positiveFeedbackCount())
                .negativeFeedbackCount(view.negativeFeedbackCount())
                .backtestCount(view.backtestCount())
                .completedBacktestCount(view.completedBacktestCount())
                .build();
        }
    }

    /** 模型/Prompt/Skill A/B 归因表现响应。 */
    @Builder
    @Schema(description = "模型/Prompt/Skill A/B 归因表现响应")
    public record VariantPerformanceResponse(
        @Schema(description = "归因键") String variantKey,
        @Schema(description = "模型展示") String modelDisplay,
        @Schema(description = "Prompt 展示") String promptDisplay,
        @Schema(description = "Skill 展示") String skillDisplay,
        @Schema(description = "样本数量") int sampleCount,
        @Schema(description = "成功率") BigDecimal successRate,
        @Schema(description = "平均耗时毫秒") BigDecimal averageDurationMs,
        @Schema(description = "最近关联业务") String latestBusinessLabel
    ) {
        /**
         * 从应用层变体表现转换为响应。
         *
         * @param view 应用层变体表现
         * @return 变体表现响应
         */
        public static VariantPerformanceResponse from(InvestmentEvolutionSummaryView.VariantPerformanceView view) {
            return VariantPerformanceResponse.builder()
                .variantKey(view.variantKey())
                .modelDisplay(view.modelDisplay())
                .promptDisplay(view.promptDisplay())
                .skillDisplay(view.skillDisplay())
                .sampleCount(view.sampleCount())
                .successRate(view.successRate())
                .averageDurationMs(view.averageDurationMs())
                .latestBusinessLabel(view.latestBusinessLabel())
                .build();
        }
    }

    /** 最近闭环运行摘要响应。 */
    @Builder
    @Schema(description = "最近闭环运行摘要响应")
    public record RecentRunResponse(
        @Schema(description = "运行标识") String runBizId,
        @Schema(description = "运行编号") String runNo,
        @Schema(description = "运行状态") String runStatus,
        @Schema(description = "报告标识") String reportBizId,
        @Schema(description = "Mock 组合标识") String portfolioBizId,
        @Schema(description = "Prompt 展示") String promptDisplay,
        @Schema(description = "质量分") BigDecimal qualityScore,
        @Schema(description = "门禁结果") String gateResult,
        @Schema(description = "开始时间") LocalDateTime startedAt
    ) {
        /**
         * 从应用层最近运行转换为响应。
         *
         * @param view 应用层最近运行
         * @return 最近运行响应
         */
        public static RecentRunResponse from(InvestmentEvolutionSummaryView.RecentRunView view) {
            return RecentRunResponse.builder()
                .runBizId(view.runBizId())
                .runNo(view.runNo())
                .runStatus(view.runStatus())
                .reportBizId(view.reportBizId())
                .portfolioBizId(view.portfolioBizId())
                .promptDisplay(view.promptDisplay())
                .qualityScore(view.qualityScore())
                .gateResult(view.gateResult())
                .startedAt(view.startedAt())
                .build();
        }
    }

    /** 原因分布响应。 */
    @Builder
    @Schema(description = "原因分布响应")
    public record ReasonCountResponse(
        @Schema(description = "原因编码") String reasonCode,
        @Schema(description = "数量") long count
    ) {
        /**
         * 从应用层原因分布转换为响应。
         *
         * @param view 应用层原因分布
         * @return 原因分布响应
         */
        public static ReasonCountResponse from(InvestmentEvolutionSummaryView.ReasonCountView view) {
            return ReasonCountResponse.builder()
                .reasonCode(view.reasonCode())
                .count(view.count())
                .build();
        }
    }
}
