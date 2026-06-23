package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟订单应用层视图。 */
@Builder
@Schema(description = "模拟订单应用层视图")
public record MockOrderView(
    @Schema(description = "订单业务唯一标识")
    String bizId,
    @Schema(description = "订单展示编号")
    String orderNo,
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "订单方向")
    String orderSide,
    @Schema(description = "订单类型")
    String orderType,
    @Schema(description = "订单币种")
    String currency,
    @Schema(description = "委托价格")
    BigDecimal requestedPrice,
    @Schema(description = "委托数量")
    BigDecimal requestedQuantity,
    @Schema(description = "委托金额")
    BigDecimal requestedAmount,
    @Schema(description = "成交数量")
    BigDecimal executedQuantity,
    @Schema(description = "成交金额")
    BigDecimal executedAmount,
    @Schema(description = "交易费用")
    BigDecimal feeAmount,
    @Schema(description = "订单状态")
    String status,
    @Schema(description = "完成时间（北京时间）")
    LocalDateTime completedAt
) {
}
