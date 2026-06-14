package com.example.dzcom.application.dto.account;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** 角色定义及其有效权限的接口视图。 */
@Builder
public record RoleView(
    String roleCode,
    String roleName,
    String description,
    String roleType,
    boolean enabled,
    Set<String> permissions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
