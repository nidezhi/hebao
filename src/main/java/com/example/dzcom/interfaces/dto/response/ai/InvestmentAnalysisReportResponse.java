package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资分析报告响应。 */
@Builder
@Schema(description = "投资分析报告响应")
public record InvestmentAnalysisReportResponse(
    @Schema(description = "报告业务 ID") String bizId,
    @Schema(description = "本次分析请求 ID") String requestId,
    @Schema(description = "分析提供方编码", example = "LOCAL_RULE") String providerCode,
    @Schema(description = "模型编码", example = "local-rule-v1") String modelCode,
    @Schema(description = "市场范围", example = "CN_MAINLAND") String marketScope,
    @Schema(description = "投资主题编码") String themeCode,
    @Schema(description = "投资主题名称") String themeName,
    @Schema(description = "状态：SUCCEEDED/FAILED") String status,
    @Schema(description = """
        投资信息汇总 JSON 字符串。
        marketScope:string，市场范围；
        themeCode:string，主题编码；
        sampleCount:number，快照样本数；
        newsCount:number，相关新闻数；
        averageReturn:number，平均收益率，小数形式；
        averageMomentum:number，平均动量；
        averageHeat:number，加权资讯热度；
        dataQualityScore:number，数据质量分，0-1；
        dataQualityLevel:string，允许值 HIGH/MEDIUM/LOW；
        latestSnapshotTime:datetime，最近快照时间，北京时间；
        recentNews:array，近期新闻数组，每项包含 title/summary/publishTime/sourceCode。
        """) String investmentSummary,
    @Schema(description = """
        趋势分析 JSON 字符串。
        direction:string，趋势方向，允许值 UP/NEUTRAL/DOWN；
        averageReturn:number，平均收益率；
        averageMomentum:number，平均动量；
        newsHeat:number，相关新闻数量；
        weightedHeatScore:number，加权资讯热度分；
        dataQualityScore:number，数据质量分；
        lookbackDays:number，回看天数。
        """) String trend,
    @Schema(description = """
        投资方案 JSON 字符串。
        planType:string，方案类型，当前为 REFERENCE_ALLOCATION；
        suggestedAction:string，建议动作；
        referenceAllocationRate:number，参考配置比例，0-1；
        referenceAllocationAmount:number，参考配置金额；
        dataQualityLevel:string，数据质量等级；
        rebalanceRule:string，再平衡规则；
        riskNotice:string，风险提示。
        """) String investmentPlan,
    @Schema(description = """
        模拟收益 JSON 字符串。
        initialCapital:number，初始资金；
        allocationRate:number，参考配置比例；
        simulatedPrincipal:number，参与模拟的本金；
        estimatedProfit:number，基准情景模拟收益；
        estimatedFinalCapital:number，模拟期末资金；
        returnRate:number，使用的平均收益率；
        stressLoss:number，压力情景收益；
        optimisticProfit:number，乐观情景收益；
        assumption:string，模拟假设说明。
        """) String simulatedReturn,
    @Schema(description = """
        前端图表数据 JSON 字符串。
        series:array，快照时间序列，每项包含 time/snapshotType/returnRate/momentumScore/heatScore；
        news:array，新闻事件点，每项包含 time/title/sourceCode。
        前端可用 series 绘制收益、动量、热度折线图，用 news 叠加事件标记。
        """) String chartPayload,
    @Schema(description = "失败原因摘要") String failureReason,
    @Schema(description = "生成时间") LocalDateTime generatedAt,
    @Schema(description = "创建时间") LocalDateTime createdAt
) {
    /** 将领域对象转换为接口响应。 */
    public static InvestmentAnalysisReportResponse from(InvestmentAnalysisReport report) {
        return InvestmentAnalysisReportResponse.builder()
            .bizId(report.bizId())
            .requestId(report.requestId())
            .providerCode(report.providerCode())
            .modelCode(report.modelCode())
            .marketScope(report.marketScope())
            .themeCode(report.themeCode())
            .themeName(report.themeName())
            .status(report.status())
            .investmentSummary(report.investmentSummary())
            .trend(report.trend())
            .investmentPlan(report.investmentPlan())
            .simulatedReturn(report.simulatedReturn())
            .chartPayload(report.chartPayload())
            .failureReason(report.failureReason())
            .generatedAt(report.generatedAt())
            .createdAt(report.createdAt())
            .build();
    }
}
