package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/** 撤销用户角色的请求。 */
@Schema(description = "撤销用户角色的请求")
public record UserRoleRevokeRequest(
    @Schema(description = "用户业务标识") @NotBlank String userBizId,
    @Schema(description = "角色编码") @NotBlank String roleCode
) {
}
