package com.example.dzcom.application.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** 角色定义及其有效权限的接口视图。 */
@Builder
@Schema(description = "角色定义和有效权限应用层视图")
public record RoleView(
    @Schema(description = "角色稳定编码")
    String roleCode,
    @Schema(description = "角色展示名称")
    String roleName,
    @Schema(description = "角色业务说明")
    String description,
    @Schema(description = "角色类型")
    String roleType,
    @Schema(description = "角色是否启用")
    boolean enabled,
    @Schema(description = "角色当前有效权限编码集合")
    Set<String> permissions,
    @Schema(description = "角色创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "角色最后更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
