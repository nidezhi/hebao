package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/** 给用户分配角色的请求。 */
public record UserRoleAssignmentRequest(
    @NotBlank String userBizId,
    @NotBlank String roleCode,
    LocalDateTime effectiveTo
) {
}
