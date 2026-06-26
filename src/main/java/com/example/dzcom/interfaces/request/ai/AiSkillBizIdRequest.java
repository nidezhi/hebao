package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI Skill 业务 ID 请求。 */
@Schema(description = "AI Skill 业务 ID 请求")
public record AiSkillBizIdRequest(
    @NotBlank
    @Schema(description = "Skill 业务 ID")
    String bizId
) {
}
