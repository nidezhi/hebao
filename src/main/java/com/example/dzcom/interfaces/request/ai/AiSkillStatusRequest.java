package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI Skill 状态变更请求。 */
@Schema(description = "AI Skill 状态变更请求")
public record AiSkillStatusRequest(
    @NotBlank
    @Schema(description = "Skill 业务 ID")
    String bizId,
    @NotBlank
    @Schema(description = "目标状态：DRAFT/VALIDATING/ACTIVE/RETIRED/ARCHIVED")
    String status
) {
}
