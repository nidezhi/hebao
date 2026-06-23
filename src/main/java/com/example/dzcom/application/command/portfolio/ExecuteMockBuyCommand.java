package com.example.dzcom.application.command.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 执行模拟买入命令。 */
@Builder
@Schema(description = "执行模拟买入命令")
public record ExecuteMockBuyCommand(
    @Schema(description = "模拟组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "买入金额，不含交易费用")
    BigDecimal amount,
    @Schema(description = "客户端幂等键，用于避免重复提交")
    String idempotencyKey
) {
}
