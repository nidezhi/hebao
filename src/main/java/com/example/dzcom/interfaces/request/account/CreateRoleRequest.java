package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/** 创建自定义角色请求。 */
@Schema(description = "创建自定义角色请求")
public record CreateRoleRequest(
    @Schema(description = "角色编码，首字母大写，允许数字与下划线", example = "CUSTOM_USER") @NotBlank
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,63}$", message = "角色编码必须为大写字母、数字或下划线")
    String roleCode,
    @Schema(description = "角色名称", example = "自定义用户角色") @NotBlank @Size(max = 128) String roleName,
    @Schema(description = "角色说明") @Size(max = 512) String description
) {
}
