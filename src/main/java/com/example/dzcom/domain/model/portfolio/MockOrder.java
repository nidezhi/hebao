package com.example.dzcom.domain.model.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟订单领域对象。 */
@Builder(toBuilder = true)
@Schema(description = "模拟订单领域对象")
public record MockOrder(
    @Schema(description = "订单业务唯一标识")
    String bizId,
    @Schema(description = "订单展示编号")
    String orderNo,
    @Schema(description = "客户端幂等键")
    String idempotencyKey,
    @Schema(description = "下单用户业务标识")
    String userBizId,
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "执行渠道编码，模拟交易固定为 SIMULATOR")
    String channelCode,
    @Schema(description = "订单方向：BUY/SELL/SUBSCRIBE/REDEEM")
    String orderSide,
    @Schema(description = "订单类型：MARKET/LIMIT/AMOUNT")
    String orderType,
    @Schema(description = "订单币种")
    String currency,
    @Schema(description = "委托价格")
    BigDecimal requestedPrice,
    @Schema(description = "委托数量")
    BigDecimal requestedQuantity,
    @Schema(description = "委托金额")
    BigDecimal requestedAmount,
    @Schema(description = "累计成交数量")
    BigDecimal executedQuantity,
    @Schema(description = "累计成交金额")
    BigDecimal executedAmount,
    @Schema(description = "累计费用")
    BigDecimal feeAmount,
    @Schema(description = "订单状态")
    String status,
    @Schema(description = "外部订单编号")
    String externalOrderId,
    @Schema(description = "拒绝原因编码")
    String rejectCode,
    @Schema(description = "拒绝原因说明")
    String rejectMessage,
    @Schema(description = "提交时间（北京时间）")
    LocalDateTime submittedAt,
    @Schema(description = "完成时间（北京时间）")
    LocalDateTime completedAt,
    @Schema(description = "乐观锁版本号")
    int version,
    @Schema(description = "记录创建时间（北京时间）")
    LocalDateTime createdAt,
    @Schema(description = "记录最后更新时间（北京时间）")
    LocalDateTime updatedAt,
    @Schema(description = "创建操作者业务标识")
    String createdBy,
    @Schema(description = "逻辑删除标记：0未删除、1已删除")
    int deleted
) {
}
