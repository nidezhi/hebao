package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟成交应用层视图。 */
@Builder
@Schema(description = "模拟成交应用层视图")
public record TradeExecutionView(
    @Schema(description = "成交业务唯一标识")
    String bizId,
    @Schema(description = "成交展示编号")
    String executionNo,
    @Schema(description = "订单业务唯一标识")
    String orderBizId,
    @Schema(description = "成交价格")
    BigDecimal executionPrice,
    @Schema(description = "成交数量")
    BigDecimal executionQuantity,
    @Schema(description = "成交金额")
    BigDecimal executionAmount,
    @Schema(description = "成交费用")
    BigDecimal feeAmount,
    @Schema(description = "成交时间（北京时间）")
    LocalDateTime executedAt
) {
}
