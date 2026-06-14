package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 更新角色名称和说明请求。 */
public record UpdateRoleRequest(
    @NotBlank String roleCode,
    @NotBlank @Size(max = 128) String roleName,
    @Size(max = 512) String description
) {
}
