package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 给用户分配角色的请求。 */
@Schema(description = "给用户分配角色的请求，可选指定角色失效时间")
public record UserRoleAssignmentRequest(
    @Schema(description = "用户业务标识") @NotBlank String userBizId,
    @Schema(description = "角色编码") @NotBlank String roleCode,
    @Schema(description = "角色失效时间（UTC），可选") LocalDateTime effectiveTo
) {
}
