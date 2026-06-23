package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/** 保存 AI Prompt 模板版本命令。 */
@Builder
@Schema(description = "保存 AI Prompt 模板版本命令")
public record SaveAiPromptTemplateCommand(
    @Schema(description = "Prompt跨版本稳定编码")
    String promptCode,
    @Schema(description = "Prompt版本号")
    String promptVersion,
    @Schema(description = "使用场景")
    String scenario,
    @Schema(description = "模板展示名称")
    String templateName,
    @Schema(description = "模板内容")
    String templateContent,
    @Schema(description = "生命周期状态")
    String status,
    @Schema(description = "模板说明")
    String description,
    @Schema(description = "变量定义集合")
    List<AiPromptVariableCommand> variables,
    @Schema(description = "输出Schema集合")
    List<AiPromptOutputSchemaCommand> outputSchemas
) {
}
