package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/** 更新角色名称和说明请求。 */
@Schema(description = "更新角色名称和说明请求")
public record UpdateRoleRequest(
    @Schema(description = "角色编码", example = "USER") @NotBlank String roleCode,
    @Schema(description = "角色名称") @NotBlank @Size(max = 128) String roleName,
    @Schema(description = "角色说明") @Size(max = 512) String description
) {
}
