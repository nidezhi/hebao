package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 保存 AI 模型业务场景挂靠配置命令。 */
@Builder
@Schema(description = "保存 AI 模型业务场景挂靠配置命令")
public record SaveAiModelBindingCommand(
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
    Boolean enabled,
    @Schema(description = "场景级模型参数 JSON")
    String config,
    @Schema(description = "配置说明")
    String description
) {
}
