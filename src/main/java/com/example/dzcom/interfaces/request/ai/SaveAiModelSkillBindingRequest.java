package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存 AI 模型 Skill 绑定请求。 */
@Schema(description = "保存 AI 模型 Skill 绑定请求")
public record SaveAiModelSkillBindingRequest(
    @NotBlank
    @Schema(description = "模型业务 ID")
    String modelBizId,
    @NotBlank
    @Schema(description = "Skill 业务 ID")
    String skillBizId,
    @NotBlank
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "优先级，数值越小越优先")
    Integer priority,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "场景级绑定配置 JSON")
    String config,
    @Schema(description = "绑定说明")
    String description
) {
}
