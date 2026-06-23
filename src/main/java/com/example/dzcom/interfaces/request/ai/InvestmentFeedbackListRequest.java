package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** 投资反馈分页查询请求。 */
@Schema(description = "投资反馈分页查询请求")
public record InvestmentFeedbackListRequest(
    @Schema(description = "反馈目标类型")
    String targetType,
    @Schema(description = "反馈目标业务标识")
    String targetBizId,
    @Schema(description = "关联投资报告业务标识")
    String reportBizId,
    @Schema(description = "Prompt稳定编码")
    String promptCode,
    @Schema(description = "Prompt版本")
    String promptVersion,
    @Schema(description = "关联回测结果业务标识")
    String backtestBizId,
    @Schema(description = "反馈动作")
    String feedbackAction,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页")
    Integer page,
    @Schema(description = "每页条数，1-100")
    Integer size,
    @Schema(description = "排序字段：createdAt/targetType/feedbackAction/promptCode/promptVersion")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
