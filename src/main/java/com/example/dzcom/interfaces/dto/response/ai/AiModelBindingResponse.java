package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiModelBindingView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型挂靠配置响应。 */
@Builder
@Schema(description = "AI 模型挂靠配置响应")
public record AiModelBindingResponse(
    @Schema(description = "配置业务唯一标识")
    String bizId,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "业务场景展示名称")
    String scenarioName,
    @Schema(description = "模型编码")
    String modelCode,
    @Schema(description = "模型提供方")
    String providerCode,
    @Schema(description = "环境编码")
    String environment,
    @Schema(description = "是否启用")
    boolean enabled,
    @Schema(description = "场景级配置 JSON")
    String config,
    @Schema(description = "配置说明")
    String description,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
    /** 将应用层视图转换为接口响应。 */
    public static AiModelBindingResponse from(AiModelBindingView view) {
        return AiModelBindingResponse.builder()
            .bizId(view.bizId())
            .scenarioCode(view.scenarioCode())
            .scenarioName(view.scenarioName())
            .modelCode(view.modelCode())
            .providerCode(view.providerCode())
            .environment(view.environment())
            .enabled(view.enabled())
            .config(view.config())
            .description(view.description())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .build();
    }
}
