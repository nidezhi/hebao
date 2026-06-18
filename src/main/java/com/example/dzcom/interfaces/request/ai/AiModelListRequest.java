package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 模型分页请求。 */
@Schema(description = "AI 模型分页请求")
public record AiModelListRequest(
    @Schema(description = "模型编码", example = "investment-analysis")
    String modelCode,
    @Schema(description = "模型类型", example = "ANALYSIS")
    String modelType,
    @Schema(description = "提供方", example = "LOCAL_RULE")
    String provider,
    @Schema(description = "状态", example = "ACTIVE")
    String status,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：updatedAt/modelCode/modelVersion/modelType/provider/status/activatedAt",
        example = "updatedAt")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
