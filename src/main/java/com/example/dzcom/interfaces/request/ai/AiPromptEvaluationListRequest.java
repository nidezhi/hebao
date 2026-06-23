package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI Prompt 评估分页查询请求。 */
@Schema(description = "AI Prompt 评估分页查询请求")
public record AiPromptEvaluationListRequest(
    @Schema(description = "Prompt稳定编码")
    String promptCode,
    @Schema(description = "Prompt版本")
    String promptVersion,
    @Schema(description = "使用场景")
    String scenario,
    @Schema(description = "关联回测结果业务标识")
    String backtestBizId,
    @Schema(description = "关联反馈业务标识")
    String feedbackBizId,
    @Schema(description = "复核状态")
    String reviewStatus,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页")
    Integer page,
    @Schema(description = "每页条数，1-100")
    Integer size,
    @Schema(description = "排序字段：evaluatedAt/promptCode/promptVersion/scenario/score/reviewStatus")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
