package com.example.dzcom.domain.model.account;

import lombok.Builder;

import java.time.LocalDateTime;

/** 角色权限映射领域对象。 */
@Builder(toBuilder = true)
public record RolePermission(
    String bizId,
    String roleCode,
    String permissionCode,
    LocalDateTime createdAt,
    String createdBy,
    int deleted,
    LocalDateTime deletedAt
) {
}
