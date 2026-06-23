package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 输出 Schema 领域对象。 */
@Builder
@Schema(description = "AI Prompt 输出 Schema 领域对象")
public record AiPromptOutputSchema(
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
