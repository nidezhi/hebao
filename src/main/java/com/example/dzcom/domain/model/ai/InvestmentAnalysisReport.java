package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 包含投资汇总、趋势、方案、模拟收益和图表数据的可追溯分析报告。 */
@Builder
@Schema(description = "可追溯投资分析报告领域对象")
public record InvestmentAnalysisReport(
    @Schema(description = "报告业务唯一标识")
    String bizId,
    @Schema(description = "单次分析请求追踪标识")
    String requestId,
    @Schema(description = "实际执行分析的提供方编码")
    String providerCode,
    @Schema(description = "本次分析使用的模型或规则编码")
    String modelCode,
    @Schema(description = "报告覆盖的市场范围")
    String marketScope,
    @Schema(description = "投资主题稳定编码")
    String themeCode,
    @Schema(description = "投资主题展示名称")
    String themeName,
    @Schema(description = "报告生成状态")
    String status,
    @Schema(description = "报告可信等级：HIGH_CONFIDENCE/MEDIUM_CONFIDENCE/LOW_CONFIDENCE/UNUSABLE")
    String confidenceLevel,
    @Schema(description = "报告输入数据质量分，0-1")
    BigDecimal dataQualityScore,
    @Schema(description = "数据质量门禁 JSON，说明是否通过、降级原因和前端提示")
    String dataQualityGate,
    @Schema(description = "投资数据和近期资讯汇总 JSON")
    String investmentSummary,
    @Schema(description = "收益方向与新闻热度趋势 JSON")
    String trend,
    @Schema(description = "参考投资动作与风险提示 JSON")
    String investmentPlan,
    @Schema(description = "模拟收益计算结果 JSON")
    String simulatedReturn,
    @Schema(description = "前端图表结构化数据 JSON")
    String chartPayload,
    @Schema(description = "脱敏后的分析输入快照 JSON")
    String promptSnapshot,
    @Schema(description = "脱敏后的模型对话快照 JSON，包含请求消息摘要和响应摘要")
    String chatSnapshot,
    @Schema(description = "失败原因摘要，成功时为空")
    String failureReason,
    @Schema(description = "报告生成时间，北京时间")
    LocalDateTime generatedAt,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt
) {
}
