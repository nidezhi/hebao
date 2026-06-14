package com.example.dzcom.application.service.account;

import java.util.Set;

/** 当前请求的认证与授权操作者。 */
public record CurrentOperator(
    String userBizId,
    String sessionToken,
    Set<String> roles,
    Set<String> permissions
) {
    public CurrentOperator {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }

    /**
     * 执行 has role 处理。
     *
     * @param role role 参数
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * 判断当前操作者是否具备指定权限。
     *
     * @param permission 权限编码
     * @return 具备权限时返回 true
     * @author dz
     * @date 2026-06-14
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
