package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiPromptTemplateView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/** AI Prompt 模板响应。 */
@Builder
@Schema(description = "AI Prompt 模板响应")
public record AiPromptTemplateResponse(
    @Schema(description = "Prompt模板业务唯一标识")
    String bizId,
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
    List<AiPromptVariableResponse> variables,
    @Schema(description = "输出Schema集合")
    List<AiPromptOutputSchemaResponse> outputSchemas,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt,
    @Schema(description = "创建操作者")
    String createdBy,
    @Schema(description = "更新操作者")
    String updatedBy
) {
    /** 从应用层视图转换为接口响应。 */
    public static AiPromptTemplateResponse from(AiPromptTemplateView view) {
        return AiPromptTemplateResponse.builder()
            .bizId(view.bizId())
            .promptCode(view.promptCode())
            .promptVersion(view.promptVersion())
            .scenario(view.scenario())
            .templateName(view.templateName())
            .templateContent(view.templateContent())
            .status(view.status())
            .description(view.description())
            .variables(view.variables().stream().map(AiPromptVariableResponse::from).toList())
            .outputSchemas(view.outputSchemas().stream().map(AiPromptOutputSchemaResponse::from).toList())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .createdBy(view.createdBy())
            .updatedBy(view.updatedBy())
            .build();
    }
}
