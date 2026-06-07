package com.example.dzcom.application.command.account;

import lombok.Builder;

/** 注册用例命令，由接口层完成协议转换后提交给应用服务。 */
@Builder
public record RegisterCommand(
    String username,
    String password,
    String email,
    String phone,
    String nickname,
    String initialRole
) {
}
