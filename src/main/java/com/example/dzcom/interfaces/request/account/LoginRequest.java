package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/** 登录请求，账号可为用户名、邮箱或手机号。 */
@Builder
public record LoginRequest(@NotBlank String account, @NotBlank String password) {
}
