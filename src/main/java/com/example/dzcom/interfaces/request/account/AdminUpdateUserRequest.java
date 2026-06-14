package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 管理端更新指定用户登录标识的请求。
 */
public record AdminUpdateUserRequest(
    @NotBlank String bizId,
    @Email String email,
    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone
) {
}
