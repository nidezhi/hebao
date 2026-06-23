package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.MockRebalanceExecutionView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/** 模拟再平衡执行结果响应。 */
@Builder
@Schema(description = "模拟再平衡执行结果响应")
public record MockRebalanceExecutionResponse(
    @Schema(description = "本次调仓生成的订单执行结果")
    List<MockOrderExecutionResponse> executions,
    @Schema(description = "调仓后的模拟组合详情")
    MockPortfolioResponse portfolio
) {
    /** 从应用层视图转换为接口响应。 */
    public static MockRebalanceExecutionResponse from(MockRebalanceExecutionView view) {
        return MockRebalanceExecutionResponse.builder()
            .executions(view.executions().stream().map(MockOrderExecutionResponse::from).toList())
            .portfolio(MockPortfolioResponse.from(view.portfolio()))
            .build();
    }
}
