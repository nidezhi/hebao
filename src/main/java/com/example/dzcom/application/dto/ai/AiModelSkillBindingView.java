package com.example.dzcom.application.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型 Skill 绑定应用层视图。 */
@Builder
@Schema(description = "AI 模型 Skill 绑定应用层视图")
public record AiModelSkillBindingView(
    @Schema(description = "绑定业务唯一标识")
    String bizId,
    @Schema(description = "模型业务 ID")
    String modelBizId,
    @Schema(description = "模型编码")
    String modelCode,
    @Schema(description = "模型版本")
    String modelVersion,
    @Schema(description = "Skill 业务 ID")
    String skillBizId,
    @Schema(description = "Skill 编码")
    String skillCode,
    @Schema(description = "Skill 版本")
    String skillVersion,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "优先级")
    Integer priority,
    @Schema(description = "是否启用")
    boolean enabled,
    @Schema(description = "场景级绑定配置 JSON")
    String config,
    @Schema(description = "绑定说明")
    String description,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
}
