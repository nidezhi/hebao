package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 模拟买入执行结果响应。 */
@Builder
@Schema(description = "模拟买入执行结果响应")
public record MockOrderExecutionResponse(
    @Schema(description = "模拟订单")
    MockOrderResponse order,
    @Schema(description = "模拟成交")
    TradeExecutionResponse execution,
    @Schema(description = "成交后的模拟组合详情")
    MockPortfolioResponse portfolio
) {
    /** 从应用层视图转换为接口响应。 */
    public static MockOrderExecutionResponse from(MockOrderExecutionView view) {
        return MockOrderExecutionResponse.builder()
            .order(MockOrderResponse.from(view.order()))
            .execution(TradeExecutionResponse.from(view.execution()))
            .portfolio(MockPortfolioResponse.from(view.portfolio()))
            .build();
    }
}
