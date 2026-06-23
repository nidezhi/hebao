package com.example.dzcom.domain.model.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟成交领域对象。 */
@Builder
@Schema(description = "模拟成交领域对象")
public record TradeExecution(
    @Schema(description = "成交业务唯一标识")
    String bizId,
    @Schema(description = "成交展示编号")
    String executionNo,
    @Schema(description = "订单业务唯一标识")
    String orderBizId,
    @Schema(description = "用户业务唯一标识")
    String userBizId,
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "执行渠道编码")
    String channelCode,
    @Schema(description = "外部成交编号")
    String externalExecutionId,
    @Schema(description = "成交价格")
    BigDecimal executionPrice,
    @Schema(description = "成交数量")
    BigDecimal executionQuantity,
    @Schema(description = "成交金额")
    BigDecimal executionAmount,
    @Schema(description = "本笔成交费用")
    BigDecimal feeAmount,
    @Schema(description = "成交时间（北京时间）")
    LocalDateTime executedAt,
    @Schema(description = "记录创建时间（北京时间）")
    LocalDateTime createdAt
) {
}
