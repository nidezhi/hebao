package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟组合持仓应用层视图。 */
@Builder
@Schema(description = "模拟组合持仓应用层视图")
public record PositionView(
    @Schema(description = "持仓业务唯一标识")
    String bizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "持仓方向：LONG/SHORT")
    String positionSide,
    @Schema(description = "当前持仓数量")
    BigDecimal quantity,
    @Schema(description = "当前可交易数量")
    BigDecimal availableQuantity,
    @Schema(description = "平均持仓成本")
    BigDecimal averageCost,
    @Schema(description = "持仓成本金额")
    BigDecimal costAmount,
    @Schema(description = "累计已实现盈亏")
    BigDecimal realizedProfit,
    @Schema(description = "最近一次影响该持仓的成交时间（北京时间）")
    LocalDateTime lastTradeAt
) {
}
