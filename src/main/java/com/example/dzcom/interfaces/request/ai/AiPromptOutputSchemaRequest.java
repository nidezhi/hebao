package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI Prompt 输出 Schema 请求。 */
@Schema(description = "AI Prompt 输出 Schema 请求")
public record AiPromptOutputSchemaRequest(
    @NotBlank
    @Schema(description = "Schema版本", example = "v1")
    String schemaVersion,
    @NotBlank
    @Schema(description = "输出JSON Schema字符串")
    String schemaJson
) {
}
