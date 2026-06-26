package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存 AI Skill 请求。 */
@Schema(description = "保存 AI Skill 请求")
public record SaveAiSkillRequest(
    @NotBlank
    @Schema(description = "Skill 编码")
    String skillCode,
    @NotBlank
    @Schema(description = "Skill 版本")
    String skillVersion,
    @NotBlank
    @Schema(description = "Skill 名称")
    String skillName,
    @NotBlank
    @Schema(description = "Skill 类型：DATA_SOURCE_DISCOVERY/PROMPT_GOVERNANCE等")
    String skillType,
    @Schema(description = "生命周期状态：DRAFT/VALIDATING/ACTIVE/RETIRED/ARCHIVED")
    String status,
    @NotBlank
    @Schema(description = "Skill 指令内容")
    String instructionContent,
    @Schema(description = "输入 JSON Schema")
    String inputSchema,
    @Schema(description = "输出 JSON Schema")
    String outputSchema,
    @Schema(description = "评估策略 JSON")
    String evaluationPolicy,
    @Schema(description = "Skill 说明")
    String description
) {
}
