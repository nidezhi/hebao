package com.example.dzcom.application.command.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 注册用例命令，由接口层完成协议转换后提交给应用服务。 */
@Builder
@Schema(description = "账户注册应用层命令")
public record RegisterCommand(
    @Schema(description = "用户登录名")
    String username,
    @Schema(description = "注册原始密码，仅在注册用例内短暂使用")
    String password,
    @Schema(description = "用户邮箱登录标识")
    String email,
    @Schema(description = "用户手机号登录标识")
    String phone,
    @Schema(description = "用户展示昵称")
    String nickname,
    @Schema(description = "注册时分配的初始角色编码")
    String initialRole
) {
}
