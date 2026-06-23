package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiPromptOutputSchemaView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 输出 Schema 响应。 */
@Builder
@Schema(description = "AI Prompt 输出 Schema 响应")
public record AiPromptOutputSchemaResponse(
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
    /** 从应用层视图转换为接口响应。 */
    public static AiPromptOutputSchemaResponse from(AiPromptOutputSchemaView view) {
        return AiPromptOutputSchemaResponse.builder()
            .bizId(view.bizId())
            .promptBizId(view.promptBizId())
            .schemaVersion(view.schemaVersion())
            .schemaJson(view.schemaJson())
            .createdAt(view.createdAt())
            .build();
    }
}
