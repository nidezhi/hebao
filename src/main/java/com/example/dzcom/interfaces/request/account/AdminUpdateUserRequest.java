package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 管理端更新指定用户登录标识的请求。
 */
@Schema(description = "管理端更新指定用户登录标识的请求")
public record AdminUpdateUserRequest(
    @Schema(description = "用户业务标识", example = "usr_01Hxxx") @NotBlank String bizId,
    @Schema(description = "邮箱", example = "alice@example.com") @Email String email,
    @Schema(description = "手机号，含国家码", example = "+8613800000000") @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone
) {
}
