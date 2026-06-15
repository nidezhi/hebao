package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 公开注册请求，密码强度和登录标识格式在接口边界先行校验。 */
@Schema(description = "公开用户注册请求")
@Builder
public record RegisterRequest(
    @Schema(description = "用户名，4-32 字符", example = "alice") @NotBlank @Size(min = 4, max = 32) String username,
    @Schema(description = "密码，8-72 字符，必须包含字母和数字", example = "P@ssw0rd123") @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "必须包含字母和数字")
    String password,
    @Schema(description = "邮箱", example = "alice@example.com") @Email String email,
    @Schema(description = "手机号，含国家码", example = "+8613800000000") @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone,
    @Schema(description = "昵称", example = "小张") @Size(max = 64) String nickname
) {
}
