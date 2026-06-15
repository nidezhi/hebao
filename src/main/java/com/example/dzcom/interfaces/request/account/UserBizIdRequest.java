package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 管理端按用户业务标识执行操作的请求。
 */
@Schema(description = "按用户业务标识执行操作的请求")
public record UserBizIdRequest(@Schema(description = "用户业务标识", example = "usr_01Hxxx") @NotBlank String bizId) {
}
