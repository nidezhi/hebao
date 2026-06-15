package com.example.dzcom.domain.model.account;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 角色权限映射领域对象。 */
@Schema(description = "角色权限映射领域对象")
@Builder(toBuilder = true)
public record RolePermission(
    @Schema(description = "映射业务标识") String bizId,
    @Schema(description = "角色编码") String roleCode,
    @Schema(description = "权限编码") String permissionCode,
    @Schema(description = "创建时间（UTC）") LocalDateTime createdAt,
    @Schema(description = "创建者标识") String createdBy,
    @Schema(description = "逻辑删除标记（0/1）") int deleted,
    @Schema(description = "删除时间（UTC）") LocalDateTime deletedAt
) {
}
