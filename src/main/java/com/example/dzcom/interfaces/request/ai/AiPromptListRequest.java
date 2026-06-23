package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI Prompt 分页查询请求。 */
@Schema(description = "AI Prompt 分页查询请求")
public record AiPromptListRequest(
    @Schema(description = "Prompt稳定编码", example = "INVESTMENT_PLAN")
    String promptCode,
    @Schema(description = "使用场景")
    String scenario,
    @Schema(description = "状态")
    String status,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：updatedAt/promptCode/promptVersion/scenario/status", example = "updatedAt")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
