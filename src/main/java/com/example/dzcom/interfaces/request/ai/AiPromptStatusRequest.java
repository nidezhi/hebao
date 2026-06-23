package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI Prompt 状态变更请求。 */
@Schema(description = "AI Prompt 状态变更请求")
public record AiPromptStatusRequest(
    @NotBlank
    @Schema(description = "Prompt模板业务唯一标识")
    String bizId,
    @NotBlank
    @Schema(description = "目标状态：DRAFT/VALIDATING/ACTIVE/RETIRED")
    String status
) {
}
