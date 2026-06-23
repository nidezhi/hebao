package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 保存 AI Prompt 评估命令。 */
@Builder
@Schema(description = "保存 AI Prompt 评估命令")
public record SaveAiPromptEvaluationCommand(
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
    @Schema(description = "综合评分")
    BigDecimal score,
    @Schema(description = "评分详情 JSON")
    String scoreDetail,
    @Schema(description = "复核状态")
    String reviewStatus
) {
}
