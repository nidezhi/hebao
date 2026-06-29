package com.example.dzcom.interfaces.dto.response.system;

import com.example.dzcom.application.dto.system.SystemConfigView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 系统配置响应。 */
@Builder
@Schema(description = "系统配置响应")
public record SystemConfigResponse(
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
    /** 转换应用层视图为接口响应。 */
    public static SystemConfigResponse from(SystemConfigView view) {
        return SystemConfigResponse.builder()
            .bizId(view.bizId())
            .configGroup(view.configGroup())
            .configKey(view.configKey())
            .environment(view.environment())
            .valueType(view.valueType())
            .configValue(view.configValue())
            .displayValue(view.displayValue())
            .description(view.description())
            .status(view.status())
            .version(view.version())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .build();
    }
}
