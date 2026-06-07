package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

/** 管理端创建用户请求，复用公开注册的身份和密码规则。 */
@Builder
public record AdminCreateUserRequest(
    @NotBlank @Size(min = 4, max = 32) String username,
    @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "必须包含字母和数字")
    String password,
    @Email String email,
    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "手机号格式不正确") String phone,
    @Size(max = 64) String nickname,
    AccountStatus status
) {
}
