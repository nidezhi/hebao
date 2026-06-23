package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Map;

/** AI Prompt 预览命令。 */
@Builder
@Schema(description = "AI Prompt 预览命令")
public record AiPromptPreviewCommand(
    @Schema(description = "Prompt模板业务唯一标识")
    String promptBizId,
    @Schema(description = "Prompt跨版本稳定编码")
    String promptCode,
    @Schema(description = "Prompt版本号")
    String promptVersion,
    @Schema(description = "预览变量值")
    Map<String, String> variables
) {
}
