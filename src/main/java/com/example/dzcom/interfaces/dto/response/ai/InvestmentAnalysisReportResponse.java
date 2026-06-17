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
    @Schema(description = "投资信息汇总 JSON") String investmentSummary,
    @Schema(description = "趋势分析 JSON") String trend,
    @Schema(description = "投资方案 JSON") String investmentPlan,
    @Schema(description = "模拟收益 JSON") String simulatedReturn,
    @Schema(description = "前端图表数据 JSON") String chartPayload,
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
