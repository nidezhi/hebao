package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 保存 AI 模型 Skill 绑定命令。 */
@Builder
@Schema(description = "保存 AI 模型 Skill 绑定命令")
public record SaveAiModelSkillBindingCommand(
    @Schema(description = "模型业务 ID")
    String modelBizId,
    @Schema(description = "Skill 业务 ID")
    String skillBizId,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "优先级")
    Integer priority,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "场景级绑定配置 JSON")
    String config,
    @Schema(description = "绑定说明")
    String description
) {
}
