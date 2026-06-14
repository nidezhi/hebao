package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 创建自定义角色请求。 */
public record CreateRoleRequest(
    @NotBlank
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,63}$", message = "角色编码必须为大写字母、数字或下划线")
    String roleCode,
    @NotBlank @Size(max = 128) String roleName,
    @Size(max = 512) String description
) {
}
