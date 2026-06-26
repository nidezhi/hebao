package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 模型挂靠配置列表请求。 */
@Schema(description = "AI 模型挂靠配置列表请求")
public record AiModelBindingListRequest(
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "模型编码")
    String modelCode,
    @Schema(description = "模型提供方")
    String providerCode,
    @Schema(description = "环境编码")
    String environment,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "页码，从 1 开始")
    Integer page,
    @Schema(description = "每页数量")
    Integer size,
    @Schema(description = "排序字段：updatedAt/scenarioCode/modelCode/providerCode/environment/enabled")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
