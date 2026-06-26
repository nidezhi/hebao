package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型实例与 Skill 版本的关联领域对象。 */
@Builder(toBuilder = true)
@Schema(description = "AI 模型实例与 Skill 版本关联领域对象")
public record AiModelSkillBinding(
    @Schema(description = "绑定业务唯一标识")
    String bizId,
    @Schema(description = "模型业务唯一标识")
    String modelBizId,
    @Schema(description = "模型稳定编码")
    String modelCode,
    @Schema(description = "模型版本")
    String modelVersion,
    @Schema(description = "Skill 业务唯一标识")
    String skillBizId,
    @Schema(description = "Skill 稳定编码")
    String skillCode,
    @Schema(description = "Skill 版本")
    String skillVersion,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "优先级，数值越小越优先")
    Integer priority,
    @Schema(description = "是否启用")
    boolean enabled,
    @Schema(description = "场景级绑定配置 JSON")
    String config,
    @Schema(description = "绑定说明")
    String description,
    @Schema(description = "创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间，北京时间")
    LocalDateTime updatedAt,
    @Schema(description = "创建人")
    String createdBy,
    @Schema(description = "更新人")
    String updatedBy
) {
}
