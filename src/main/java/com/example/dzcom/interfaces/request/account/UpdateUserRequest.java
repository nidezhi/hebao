package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 更新邮箱和手机号登录标识的请求。 */
@Schema(description = "更新用户邮箱和手机号请求")
@Builder
public record UpdateUserRequest(
    @Schema(description = "邮箱", example = "alice@example.com") @Email String email,
    @Schema(description = "手机号，含国家码", example = "+8613800000000") @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone
) {
}
