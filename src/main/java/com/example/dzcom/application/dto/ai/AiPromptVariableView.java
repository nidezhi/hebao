package com.example.dzcom.application.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 变量定义应用层视图。 */
@Builder
@Schema(description = "AI Prompt 变量定义应用层视图")
public record AiPromptVariableView(
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
    @Schema(description = "预览输入类型：text/textarea/json")
    String previewValueType,
    @Schema(description = "预览默认值")
    String previewDefaultValue,
    @Schema(description = "预览示例值")
    String previewExampleValue,
    @Schema(description = "创建时间")
    LocalDateTime createdAt
) {
}
