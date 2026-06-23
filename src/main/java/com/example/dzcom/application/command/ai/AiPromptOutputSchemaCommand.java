package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** AI Prompt 输出 Schema 命令。 */
@Builder
@Schema(description = "AI Prompt 输出 Schema 命令")
public record AiPromptOutputSchemaCommand(
    @Schema(description = "Schema版本号")
    String schemaVersion,
    @Schema(description = "输出JSON Schema")
    String schemaJson
) {
}
