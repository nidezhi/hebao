package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 模拟买入执行结果应用层视图。 */
@Builder
@Schema(description = "模拟买入执行结果应用层视图")
public record MockOrderExecutionView(
    @Schema(description = "模拟订单")
    MockOrderView order,
    @Schema(description = "模拟成交")
    TradeExecutionView execution,
    @Schema(description = "成交后模拟组合详情")
    MockPortfolioView portfolio
) {
}
