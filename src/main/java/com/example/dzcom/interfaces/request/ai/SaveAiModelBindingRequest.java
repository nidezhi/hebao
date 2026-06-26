package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存 AI 模型挂靠配置请求。 */
@Schema(description = "保存 AI 模型挂靠配置请求")
public record SaveAiModelBindingRequest(
    @Schema(description = "业务场景编码，例如 DATA_SOURCE_DISCOVERY/AUTO_REPORT_GENERATION")
    @NotBlank
    String scenarioCode,
    @Schema(description = "业务场景展示名称")
    @NotBlank
    String scenarioName,
    @Schema(description = "模型稳定编码")
    @NotBlank
    String modelCode,
    @Schema(description = "模型提供方一致性校验编码")
    String providerCode,
    @Schema(description = "生效环境，默认 DEFAULT")
    String environment,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "场景级模型参数 JSON")
    String config,
    @Schema(description = "配置说明")
    String description
) {
}
