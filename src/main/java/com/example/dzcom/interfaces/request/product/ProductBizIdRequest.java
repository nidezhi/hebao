package com.example.dzcom.interfaces.request.product;

import jakarta.validation.constraints.NotBlank;

/**
 * 按产品业务标识执行操作的请求。
 */
public record ProductBizIdRequest(@NotBlank String bizId) {
}
