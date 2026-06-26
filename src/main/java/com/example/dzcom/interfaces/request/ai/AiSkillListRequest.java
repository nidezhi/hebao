package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI Skill 列表请求。 */
@Schema(description = "AI Skill 列表请求")
public record AiSkillListRequest(
    @Schema(description = "Skill 编码")
    String skillCode,
    @Schema(description = "Skill 类型")
    String skillType,
    @Schema(description = "状态")
    String status,
    @Schema(description = "关键词")
    String keyword,
    @Schema(description = "页码，从 1 开始")
    Integer page,
    @Schema(description = "每页数量")
    Integer size,
    @Schema(description = "排序字段：updatedAt/skillCode/skillVersion/skillType/status")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
