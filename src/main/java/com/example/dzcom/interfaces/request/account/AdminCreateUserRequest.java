package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 管理端创建用户请求，复用公开注册的身份和密码规则。 */
@Schema(description = "管理端创建用户请求")
@Builder
public record AdminCreateUserRequest(
    @Schema(description = "用户名，4-32 字符", example = "alice") @NotBlank @Size(min = 4, max = 32) String username,
    @Schema(description = "密码，8-72 字符，必须包含字母和数字", example = "P@ssw0rd123") @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "必须包含字母和数字")
    String password,
    @Schema(description = "邮箱地址", example = "alice@example.com") @Email String email,
    @Schema(description = "手机号，含国家码", example = "+8613800000000") @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone,
    @Schema(description = "昵称", example = "小张") @Size(max = 64) String nickname,
    @Schema(description = "初始账户状态，可选，默认 ACTIVE") AccountStatus status
) {
}
