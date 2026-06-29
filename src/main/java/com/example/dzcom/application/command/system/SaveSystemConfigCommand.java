package com.example.dzcom.application.command.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 保存非敏感系统配置命令。 */
@Builder
@Schema(description = "保存非敏感系统配置命令")
public record SaveSystemConfigCommand(
    @Schema(description = "配置分组编码")
    String configGroup,
    @Schema(description = "配置键名")
    String configKey,
    @Schema(description = "配置生效环境")
    String environment,
    @Schema(description = "配置值类型：STRING/NUMBER/BOOLEAN/JSON")
    String valueType,
    @Schema(description = "配置值对象，由值类型决定")
    Object configValue,
    @Schema(description = "配置用途说明")
    String description,
    @Schema(description = "配置状态：ENABLED/DISABLED")
    String status
) {
}
