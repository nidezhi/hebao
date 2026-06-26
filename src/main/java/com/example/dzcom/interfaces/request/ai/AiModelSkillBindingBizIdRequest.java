package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI 模型 Skill 绑定业务 ID 请求。 */
@Schema(description = "AI 模型 Skill 绑定业务 ID 请求")
public record AiModelSkillBindingBizIdRequest(
    @NotBlank
    @Schema(description = "绑定业务 ID")
    String bizId
) {
}
