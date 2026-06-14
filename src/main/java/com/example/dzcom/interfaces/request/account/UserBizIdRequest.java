package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理端按用户业务标识执行操作的请求。
 */
public record UserBizIdRequest(@NotBlank String bizId) {
}
