package com.example.dzcom.application.command.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 执行模拟卖出命令。 */
@Builder
@Schema(description = "执行模拟卖出命令")
public record ExecuteMockSellCommand(
    @Schema(description = "模拟组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "卖出数量")
    BigDecimal quantity,
    @Schema(description = "客户端幂等键，用于避免重复提交")
    String idempotencyKey
) {
}
