package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/** 更新邮箱和手机号登录标识的请求。 */
@Builder
public record UpdateUserRequest(
    @Email String email,
    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone
) {
}
