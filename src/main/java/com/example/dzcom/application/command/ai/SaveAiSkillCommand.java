package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 保存 AI Skill 版本命令。 */
@Builder
@Schema(description = "保存 AI Skill 版本命令")
public record SaveAiSkillCommand(
    @Schema(description = "Skill 编码")
    String skillCode,
    @Schema(description = "Skill 版本")
    String skillVersion,
    @Schema(description = "Skill 名称")
    String skillName,
    @Schema(description = "Skill 类型")
    String skillType,
    @Schema(description = "生命周期状态")
    String status,
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
