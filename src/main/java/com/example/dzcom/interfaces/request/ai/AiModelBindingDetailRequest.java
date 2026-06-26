package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AI 模型挂靠配置详情请求。 */
@Schema(description = "AI 模型挂靠配置详情请求")
public record AiModelBindingDetailRequest(
    @Schema(description = "业务场景编码")
    @NotBlank
    String scenarioCode,
    @Schema(description = "环境编码，默认 DEFAULT")
    String environment
) {
}
