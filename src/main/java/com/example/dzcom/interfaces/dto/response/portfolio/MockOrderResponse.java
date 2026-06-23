package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.MockOrderView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟订单响应。 */
@Builder
@Schema(description = "模拟订单响应")
public record MockOrderResponse(
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
    /** 从应用层视图转换为接口响应。 */
    public static MockOrderResponse from(MockOrderView view) {
        return MockOrderResponse.builder()
            .bizId(view.bizId())
            .orderNo(view.orderNo())
            .portfolioBizId(view.portfolioBizId())
            .productBizId(view.productBizId())
            .orderSide(view.orderSide())
            .orderType(view.orderType())
            .currency(view.currency())
            .requestedPrice(view.requestedPrice())
            .requestedAmount(view.requestedAmount())
            .executedQuantity(view.executedQuantity())
            .executedAmount(view.executedAmount())
            .feeAmount(view.feeAmount())
            .status(view.status())
            .completedAt(view.completedAt())
            .build();
    }
}
