package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/** 修改密码请求，必须同时提交当前密码并满足新密码强度规则。 */
@Builder
public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "必须包含字母和数字")
    String newPassword
) {
}
