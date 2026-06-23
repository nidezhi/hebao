package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.TradeExecutionView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟成交响应。 */
@Builder
@Schema(description = "模拟成交响应")
public record TradeExecutionResponse(
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
    /** 从应用层视图转换为接口响应。 */
    public static TradeExecutionResponse from(TradeExecutionView view) {
        return TradeExecutionResponse.builder()
            .bizId(view.bizId())
            .executionNo(view.executionNo())
            .orderBizId(view.orderBizId())
            .executionPrice(view.executionPrice())
            .executionQuantity(view.executionQuantity())
            .executionAmount(view.executionAmount())
            .feeAmount(view.feeAmount())
            .executedAt(view.executedAt())
            .build();
    }
}
