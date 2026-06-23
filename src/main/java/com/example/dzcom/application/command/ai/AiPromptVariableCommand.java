package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** AI Prompt 变量定义命令。 */
@Builder
@Schema(description = "AI Prompt 变量定义命令")
public record AiPromptVariableCommand(
    @Schema(description = "变量名称")
    String variableName,
    @Schema(description = "变量默认来源路径")
    String sourcePath,
    @Schema(description = "是否必填")
    Boolean required,
    @Schema(description = "变量说明")
    String description
) {
}
