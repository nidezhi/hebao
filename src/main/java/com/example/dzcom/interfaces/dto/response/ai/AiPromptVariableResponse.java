package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiPromptVariableView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 变量定义响应。 */
@Builder
@Schema(description = "AI Prompt 变量定义响应")
public record AiPromptVariableResponse(
    @Schema(description = "变量业务唯一标识")
    String bizId,
    @Schema(description = "Prompt模板业务唯一标识")
    String promptBizId,
    @Schema(description = "变量名称")
    String variableName,
    @Schema(description = "变量默认来源路径")
    String sourcePath,
    @Schema(description = "是否必填")
    boolean required,
    @Schema(description = "变量说明")
    String description,
    @Schema(description = "创建时间")
    LocalDateTime createdAt
) {
    /** 从应用层视图转换为接口响应。 */
    public static AiPromptVariableResponse from(AiPromptVariableView view) {
        return AiPromptVariableResponse.builder()
            .bizId(view.bizId())
            .promptBizId(view.promptBizId())
            .variableName(view.variableName())
            .sourcePath(view.sourcePath())
            .required(view.required())
            .description(view.description())
            .createdAt(view.createdAt())
            .build();
    }
}
