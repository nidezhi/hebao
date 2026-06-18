package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI 模型状态变更请求。 */
@Schema(description = "AI 模型状态变更请求")
public record AiModelStatusRequest(
    @NotBlank
    @Schema(description = "模型业务 ID")
    String bizId,
    @NotBlank
    @Schema(description = "目标状态：DRAFT/VALIDATING/ACTIVE/INACTIVE/ARCHIVED", example = "ACTIVE")
    String status
) {
}
