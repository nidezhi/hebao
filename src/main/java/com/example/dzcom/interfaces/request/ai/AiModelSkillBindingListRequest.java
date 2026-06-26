package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 模型 Skill 绑定列表请求。 */
@Schema(description = "AI 模型 Skill 绑定列表请求")
public record AiModelSkillBindingListRequest(
    @Schema(description = "模型业务 ID")
    String modelBizId,
    @Schema(description = "模型编码")
    String modelCode,
    @Schema(description = "Skill 编码")
    String skillCode,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "页码，从 1 开始")
    Integer page,
    @Schema(description = "每页数量")
    Integer size,
    @Schema(description = "排序字段：updatedAt/modelCode/skillCode/scenarioCode/priority/enabled")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
