package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/** 覆盖配置角色权限集合的请求。 */
public record ConfigureRolePermissionsRequest(
    @NotBlank String roleCode,
    @NotNull Set<@NotBlank String> permissions
) {
}
