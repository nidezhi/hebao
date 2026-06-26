package com.example.dzcom.application.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Skill 版本应用层视图。 */
@Builder
@Schema(description = "AI Skill 版本应用层视图")
public record AiSkillView(
    @Schema(description = "Skill 业务唯一标识")
    String bizId,
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
    String description,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
}
