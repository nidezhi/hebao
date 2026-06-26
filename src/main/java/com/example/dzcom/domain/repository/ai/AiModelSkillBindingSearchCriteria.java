package com.example.dzcom.domain.repository.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 模型 Skill 绑定分页查询条件。 */
@Schema(description = "AI 模型 Skill 绑定分页查询条件")
public record AiModelSkillBindingSearchCriteria(
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
