package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI Prompt 变量定义请求。 */
@Schema(description = "AI Prompt 变量定义请求")
public record AiPromptVariableRequest(
    @NotBlank
    @Schema(description = "变量名称，不包含 ${} 符号", example = "reportSummary")
    String variableName,
    @Schema(description = "变量默认来源路径", example = "report.summary")
    String sourcePath,
    @Schema(description = "是否必填")
    Boolean required,
    @Schema(description = "变量说明")
    String description
) {
}
