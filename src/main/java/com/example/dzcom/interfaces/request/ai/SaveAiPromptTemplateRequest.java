package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** 保存 AI Prompt 模板请求。 */
@Schema(description = "保存 AI Prompt 模板请求")
public record SaveAiPromptTemplateRequest(
    @NotBlank
    @Schema(description = "Prompt稳定编码", example = "INVESTMENT_PLAN")
    String promptCode,
    @NotBlank
    @Schema(description = "Prompt版本", example = "v1")
    String promptVersion,
    @NotBlank
    @Schema(description = "使用场景：INVESTMENT_REPORT/INVESTMENT_PLAN/RISK_AUDIT/BACKTEST_FEEDBACK")
    String scenario,
    @NotBlank
    @Schema(description = "模板展示名称")
    String templateName,
    @NotBlank
    @Schema(description = "模板内容，变量使用 ${variableName} 占位")
    String templateContent,
    @Schema(description = "状态：DRAFT/VALIDATING/ACTIVE/RETIRED", example = "DRAFT")
    String status,
    @Schema(description = "模板说明")
    String description,
    @Valid
    @Schema(description = "变量定义集合")
    List<AiPromptVariableRequest> variables,
    @Valid
    @Schema(description = "输出Schema集合")
    List<AiPromptOutputSchemaRequest> outputSchemas
) {
}
