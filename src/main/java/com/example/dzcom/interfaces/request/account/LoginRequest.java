package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 登录请求，账号可为用户名、邮箱或手机号。 */
@Schema(description = "登录请求：account 可为用户名/邮箱/手机号")
@Builder
public record LoginRequest(
	@Schema(description = "用户名/邮箱/手机号", example = "alice@example.com") @NotBlank String account,
	@Schema(description = "密码", example = "P@ssw0rd123") @NotBlank String password
) {
}
