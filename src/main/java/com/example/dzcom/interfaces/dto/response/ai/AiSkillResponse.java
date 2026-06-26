package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiSkillView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Skill 响应。 */
@Builder
@Schema(description = "AI Skill 响应")
public record AiSkillResponse(
    @Schema(description = "Skill 业务 ID")
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
    /** 将应用层视图转换为接口响应。 */
    public static AiSkillResponse from(AiSkillView view) {
        return AiSkillResponse.builder()
            .bizId(view.bizId())
            .skillCode(view.skillCode())
            .skillVersion(view.skillVersion())
            .skillName(view.skillName())
            .skillType(view.skillType())
            .status(view.status())
            .instructionContent(view.instructionContent())
            .inputSchema(view.inputSchema())
            .outputSchema(view.outputSchema())
            .evaluationPolicy(view.evaluationPolicy())
            .description(view.description())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .build();
    }
}
