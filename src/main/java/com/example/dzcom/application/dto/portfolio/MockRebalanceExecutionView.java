package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/** 模拟再平衡执行结果应用层视图。 */
@Builder
@Schema(description = "模拟再平衡执行结果应用层视图")
public record MockRebalanceExecutionView(
    @Schema(description = "本次调仓生成的订单执行结果")
    List<MockOrderExecutionView> executions,
    @Schema(description = "调仓后的模拟组合详情")
    MockPortfolioView portfolio
) {
}
