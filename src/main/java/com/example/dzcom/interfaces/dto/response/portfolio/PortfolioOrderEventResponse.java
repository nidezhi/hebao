package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.PortfolioOrderEventView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟组合订单事件响应。 */
@Builder
@Schema(description = "模拟组合订单事件响应")
public record PortfolioOrderEventResponse(
    @Schema(description = "订单事件业务唯一标识")
    String bizId,
    @Schema(description = "订单业务唯一标识")
    String orderBizId,
    @Schema(description = "订单展示编号")
    String orderNo,
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "订单方向")
    String orderSide,
    @Schema(description = "订单状态")
    String orderStatus,
    @Schema(description = "事件类型")
    String eventType,
    @Schema(description = "变更前订单状态")
    String fromStatus,
    @Schema(description = "变更后订单状态")
    String toStatus,
    @Schema(description = "事件来源")
    String eventSource,
    @Schema(description = "委托金额")
    BigDecimal requestedAmount,
    @Schema(description = "委托数量")
    BigDecimal requestedQuantity,
    @Schema(description = "成交金额")
    BigDecimal executedAmount,
    @Schema(description = "成交数量")
    BigDecimal executedQuantity,
    @Schema(description = "事件发生时间（北京时间）")
    LocalDateTime occurredAt
) {
    /** 从应用层视图转换为接口响应。 */
    public static PortfolioOrderEventResponse from(PortfolioOrderEventView view) {
        return PortfolioOrderEventResponse.builder()
            .bizId(view.bizId())
            .orderBizId(view.orderBizId())
            .orderNo(view.orderNo())
            .portfolioBizId(view.portfolioBizId())
            .productBizId(view.productBizId())
            .orderSide(view.orderSide())
            .orderStatus(view.orderStatus())
            .eventType(view.eventType())
            .fromStatus(view.fromStatus())
            .toStatus(view.toStatus())
            .eventSource(view.eventSource())
            .requestedAmount(view.requestedAmount())
            .requestedQuantity(view.requestedQuantity())
            .executedAmount(view.executedAmount())
            .executedQuantity(view.executedQuantity())
            .occurredAt(view.occurredAt())
            .build();
    }
}
