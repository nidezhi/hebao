package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 保存 AI Prompt 评估请求。 */
@Schema(description = "保存 AI Prompt 评估请求")
public record SaveAiPromptEvaluationRequest(
    @Schema(description = "Prompt模板业务标识")
    String promptBizId,
    @NotBlank
    @Schema(description = "Prompt稳定编码")
    String promptCode,
    @NotBlank
    @Schema(description = "Prompt版本")
    String promptVersion,
    @NotBlank
    @Schema(description = "使用场景")
    String scenario,
    @Schema(description = "关联回测结果业务标识")
    String backtestBizId,
    @Schema(description = "关联反馈业务标识")
    String feedbackBizId,
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Schema(description = "综合评分，0-1")
    BigDecimal score,
    @Schema(description = "评分详情 JSON")
    String scoreDetail,
    @Schema(description = "复核状态：PENDING/APPROVED/REJECTED/ARCHIVED")
    String reviewStatus
) {
}
