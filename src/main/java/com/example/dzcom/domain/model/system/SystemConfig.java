package com.example.dzcom.domain.model.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 非敏感系统配置。 */
@Schema(description = "非敏感系统配置领域对象")
@Builder(toBuilder = true)
public record SystemConfig(
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
    @Schema(description = "配置用途说明")
    String description,
    @Schema(description = "配置状态：ACTIVE/INACTIVE")
    String status,
    @Schema(description = "配置版本号")
    Integer version,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "记录更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
