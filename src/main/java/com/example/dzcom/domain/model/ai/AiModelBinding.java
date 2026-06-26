package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型业务场景挂靠配置领域对象。 */
@Builder(toBuilder = true)
@Schema(description = "AI 模型业务场景挂靠配置领域对象")
public record AiModelBinding(
    @Schema(description = "挂靠配置业务唯一标识")
    String bizId,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "业务场景展示名称")
    String scenarioName,
    @Schema(description = "模型稳定编码")
    String modelCode,
    @Schema(description = "模型提供方一致性校验编码")
    String providerCode,
    @Schema(description = "生效环境")
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
    LocalDateTime updatedAt,
    @Schema(description = "创建人")
    String createdBy,
    @Schema(description = "更新人")
    String updatedBy
) {
}
