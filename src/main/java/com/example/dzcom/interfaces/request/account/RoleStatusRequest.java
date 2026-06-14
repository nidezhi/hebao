package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 启用或停用角色请求。 */
public record RoleStatusRequest(
    @NotBlank String roleCode,
    @NotNull Boolean enabled
) {
}
