package com.example.dzcom.application.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 输出 Schema 应用层视图。 */
@Builder
@Schema(description = "AI Prompt 输出 Schema 应用层视图")
public record AiPromptOutputSchemaView(
    @Schema(description = "输出Schema业务唯一标识")
    String bizId,
    @Schema(description = "Prompt模板业务唯一标识")
    String promptBizId,
    @Schema(description = "Schema版本号")
    String schemaVersion,
    @Schema(description = "输出JSON Schema")
    String schemaJson,
    @Schema(description = "创建时间")
    LocalDateTime createdAt
) {
}
