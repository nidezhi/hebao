package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI Prompt 评估业务 ID 请求。 */
@Schema(description = "AI Prompt 评估业务 ID 请求")
public record AiPromptEvaluationBizIdRequest(
    @NotBlank
    @Schema(description = "Prompt评估业务唯一标识")
    String bizId
) {
}
