package com.example.dzcom.domain.repository.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 模型挂靠配置查询条件。 */
@Schema(description = "AI 模型挂靠配置查询条件")
public record AiModelBindingSearchCriteria(
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "模型编码")
    String modelCode,
    @Schema(description = "模型提供方编码")
    String providerCode,
    @Schema(description = "环境编码")
    String environment,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "页码")
    int page,
    @Schema(description = "每页数量")
    int size,
    @Schema(description = "排序字段")
    String sort,
    @Schema(description = "是否升序")
    boolean ascending
) {
}
