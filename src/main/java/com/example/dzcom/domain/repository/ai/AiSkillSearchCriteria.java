package com.example.dzcom.domain.repository.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI Skill 分页查询条件。 */
@Schema(description = "AI Skill 分页查询条件")
public record AiSkillSearchCriteria(
    @Schema(description = "Skill 编码")
    String skillCode,
    @Schema(description = "Skill 类型")
    String skillType,
    @Schema(description = "状态")
    String status,
    @Schema(description = "关键词")
    String keyword,
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
