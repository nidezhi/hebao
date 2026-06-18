package com.example.dzcom.application.command.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 更新邮箱和手机号登录标识的用例命令。 */
@Builder
@Schema(description = "更新用户邮箱和手机号登录标识的应用层命令")
public record UpdateIdentitiesCommand(
    @Schema(description = "更新后的邮箱登录标识")
    String email,
    @Schema(description = "更新后的手机号登录标识")
    String phone
) {
}
