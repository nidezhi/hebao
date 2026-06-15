package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 修改密码请求，必须同时提交当前密码并满足新密码强度规则。 */
@Schema(description = "修改密码请求：提供当前密码和新密码")
@Builder
public record ChangePasswordRequest(
    @Schema(description = "当前密码", example = "OldP@ssw0rd") @NotBlank String currentPassword,
    @Schema(description = "新密码，8-72 字符且包含字母和数字", example = "NewP@ssw0rd123") @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "必须包含字母和数字")
    String newPassword
) {
}
