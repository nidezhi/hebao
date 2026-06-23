package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 变量定义领域对象。 */
@Builder
@Schema(description = "AI Prompt 变量定义领域对象")
public record AiPromptVariable(
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
}
