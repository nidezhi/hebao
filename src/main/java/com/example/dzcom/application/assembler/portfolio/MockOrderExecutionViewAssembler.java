package com.example.dzcom.application.assembler.portfolio;

import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockOrderView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.dto.portfolio.TradeExecutionView;
import com.example.dzcom.domain.model.portfolio.MockOrder;
import com.example.dzcom.domain.model.portfolio.TradeExecution;
import org.springframework.stereotype.Component;

/** 模拟订单执行结果组装器。 */
@Component
public class MockOrderExecutionViewAssembler {
    /**
     * 组装模拟买入执行结果。
     *
     * @param order 模拟订单
     * @param execution 模拟成交
     * @param portfolio 成交后的组合详情
     * @return 模拟买入执行结果
     * @author dz
     * @date 2026-06-23
     */
    public MockOrderExecutionView assemble(
        MockOrder order,
        TradeExecution execution,
        MockPortfolioView portfolio
    ) {
        return MockOrderExecutionView.builder()
            .order(toOrderView(order))
            .execution(toExecutionView(execution))
            .portfolio(portfolio)
            .build();
    }

    /** 转换订单视图。 */
    private MockOrderView toOrderView(MockOrder order) {
        return assembleOrder(order);
    }

    /**
     * 单独组装模拟订单视图，用于撤单等没有成交明细的订单状态响应。
     *
     * @param order 模拟订单
     * @return 模拟订单视图
     * @author dz
     * @date 2026-06-23
     */
    public MockOrderView assembleOrder(MockOrder order) {
        return MockOrderView.builder()
            .bizId(order.bizId())
            .orderNo(order.orderNo())
            .portfolioBizId(order.portfolioBizId())
            .productBizId(order.productBizId())
            .orderSide(order.orderSide())
            .orderType(order.orderType())
            .currency(order.currency())
            .requestedPrice(order.requestedPrice())
            .requestedQuantity(order.requestedQuantity())
            .requestedAmount(order.requestedAmount())
            .executedQuantity(order.executedQuantity())
            .executedAmount(order.executedAmount())
            .feeAmount(order.feeAmount())
            .status(order.status())
            .completedAt(order.completedAt())
            .build();
    }

    /** 转换成交视图。 */
    private TradeExecutionView toExecutionView(TradeExecution execution) {
        return TradeExecutionView.builder()
            .bizId(execution.bizId())
            .executionNo(execution.executionNo())
            .orderBizId(execution.orderBizId())
            .executionPrice(execution.executionPrice())
            .executionQuantity(execution.executionQuantity())
            .executionAmount(execution.executionAmount())
            .feeAmount(execution.feeAmount())
            .executedAt(execution.executedAt())
            .build();
    }
}
