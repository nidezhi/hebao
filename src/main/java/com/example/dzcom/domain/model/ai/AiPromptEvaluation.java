package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** AI Prompt 评估领域对象。 */
@Builder
@Schema(description = "AI Prompt 评估领域对象")
public record AiPromptEvaluation(
    @Schema(description = "Prompt评估业务唯一标识")
    String bizId,
    @Schema(description = "Prompt模板业务标识")
    String promptBizId,
    @Schema(description = "Prompt稳定编码快照")
    String promptCode,
    @Schema(description = "Prompt版本快照")
    String promptVersion,
    @Schema(description = "使用场景快照")
    String scenario,
    @Schema(description = "关联回测结果业务标识")
    String backtestBizId,
    @Schema(description = "关联反馈业务标识")
    String feedbackBizId,
    @Schema(description = "综合评分，0-1")
    BigDecimal score,
    @Schema(description = "评分详情 JSON")
    String scoreDetail,
    @Schema(description = "复核状态")
    String reviewStatus,
    @Schema(description = "评估来源")
    String evaluatorType,
    @Schema(description = "评估人或任务业务标识")
    String evaluatorBizId,
    @Schema(description = "评估时间")
    LocalDateTime evaluatedAt,
    @Schema(description = "创建时间")
    LocalDateTime createdAt
) {
}
