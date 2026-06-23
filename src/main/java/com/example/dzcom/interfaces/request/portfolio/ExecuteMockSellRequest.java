package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 执行模拟卖出请求。 */
@Schema(description = "执行模拟卖出请求")
public record ExecuteMockSellRequest(
    @Schema(description = "模拟组合业务唯一标识")
    @NotBlank
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    @NotBlank
    String productBizId,
    @Schema(description = "卖出数量")
    @NotNull
    BigDecimal quantity,
    @Schema(description = "客户端幂等键，用于避免重复提交")
    String idempotencyKey
) {
}
