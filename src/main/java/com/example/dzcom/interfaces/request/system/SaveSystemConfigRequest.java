package com.example.dzcom.interfaces.request.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存系统配置请求。 */
@Schema(description = "保存系统配置请求")
public record SaveSystemConfigRequest(
    @Schema(description = "配置分组编码")
    @NotBlank
    String configGroup,
    @Schema(description = "配置键名")
    @NotBlank
    String configKey,
    @Schema(description = "配置生效环境，默认 DEFAULT")
    String environment,
    @Schema(description = "配置值类型：STRING/NUMBER/BOOLEAN/JSON")
    @NotBlank
    String valueType,
    @Schema(description = "配置值对象，由值类型决定")
    Object configValue,
    @Schema(description = "配置用途说明")
    String description,
    @Schema(description = "配置状态：ENABLED/DISABLED")
    String status
) {
}
