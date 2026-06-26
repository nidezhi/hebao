package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 查询模型启用 Skill 请求。 */
@Schema(description = "查询模型启用 Skill 请求")
public record AiModelSkillsRequest(
    @NotBlank
    @Schema(description = "模型业务 ID")
    String modelBizId
) {
}
