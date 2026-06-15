package com.example.dzcom.interfaces.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 按产品业务标识执行操作的请求。
 */
@Schema(description = "按产品业务标识执行操作的请求")
public record ProductBizIdRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String bizId
) {
}
