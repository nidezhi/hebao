package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/** 启用或停用角色请求。 */
@Schema(description = "启用或停用角色请求")
public record RoleStatusRequest(
    @Schema(description = "角色编码", example = "USER") @NotBlank String roleCode,
    @Schema(description = "是否启用") @NotNull Boolean enabled
) {
}
