package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/** 公开注册请求，密码强度和登录标识格式在接口边界先行校验。 */
@Builder
public record RegisterRequest(
    @NotBlank @Size(min = 4, max = 32) String username,
    @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "必须包含字母和数字")
    String password,
    @Email String email,
    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone,
    @Size(max = 64) String nickname
) {
}
