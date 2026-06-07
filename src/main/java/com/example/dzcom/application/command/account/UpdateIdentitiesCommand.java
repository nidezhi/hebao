package com.example.dzcom.application.command.account;

import lombok.Builder;

/** 更新邮箱和手机号登录标识的用例命令。 */
@Builder
public record UpdateIdentitiesCommand(String email, String phone) {
}
