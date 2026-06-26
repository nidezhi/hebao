package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiModelSkillBindingView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型 Skill 绑定响应。 */
@Builder
@Schema(description = "AI 模型 Skill 绑定响应")
public record AiModelSkillBindingResponse(
    @Schema(description = "绑定业务 ID")
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
    /** 将应用层视图转换为接口响应。 */
    public static AiModelSkillBindingResponse from(AiModelSkillBindingView view) {
        return AiModelSkillBindingResponse.builder()
            .bizId(view.bizId())
            .modelBizId(view.modelBizId())
            .modelCode(view.modelCode())
            .modelVersion(view.modelVersion())
            .skillBizId(view.skillBizId())
            .skillCode(view.skillCode())
            .skillVersion(view.skillVersion())
            .scenarioCode(view.scenarioCode())
            .priority(view.priority())
            .enabled(view.enabled())
            .config(view.config())
            .description(view.description())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .build();
    }
}
