package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;

/** 撤销用户角色的请求。 */
public record UserRoleRevokeRequest(
    @NotBlank String userBizId,
    @NotBlank String roleCode
) {
}
