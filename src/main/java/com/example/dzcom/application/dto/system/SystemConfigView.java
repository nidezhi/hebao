package com.example.dzcom.application.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 非敏感系统配置应用视图。 */
@Builder
@Schema(description = "非敏感系统配置应用视图")
public record SystemConfigView(
    @Schema(description = "配置业务唯一标识")
    String bizId,
    @Schema(description = "配置分组编码")
    String configGroup,
    @Schema(description = "配置键名")
    String configKey,
    @Schema(description = "配置生效环境")
    String environment,
    @Schema(description = "配置值类型：STRING/NUMBER/BOOLEAN/JSON")
    String valueType,
    @Schema(description = "配置值 JSON 字符串")
    String configValue,
    @Schema(description = "配置展示值")
    String displayValue,
    @Schema(description = "配置用途说明")
    String description,
    @Schema(description = "配置状态：ENABLED/DISABLED")
    String status,
    @Schema(description = "配置版本号")
    Integer version,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "记录更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
