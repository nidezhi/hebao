package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/** 覆盖配置角色权限集合的请求。 */
@Schema(description = "覆盖配置角色权限集合的请求")
public record ConfigureRolePermissionsRequest(
    @Schema(description = "角色编码", example = "USER") @NotBlank String roleCode,
    @Schema(description = "权限编码集合") @NotNull Set<@NotBlank String> permissions
) {
}
