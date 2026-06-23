package com.example.dzcom.domain.model.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 模拟订单状态事件领域对象，用于追加式审计订单生命周期。 */
@Builder
@Schema(description = "模拟订单状态事件领域对象")
public record OrderEvent(
    @Schema(description = "订单事件业务唯一标识")
    String bizId,
    @Schema(description = "订单业务唯一标识")
    String orderBizId,
    @Schema(description = "事件类型：CREATED/SUBMITTED/FILLED/CANCELLED等")
    String eventType,
    @Schema(description = "变更前订单状态")
    String fromStatus,
    @Schema(description = "变更后订单状态")
    String toStatus,
    @Schema(description = "事件来源：INTERNAL/CHANNEL/OPERATOR")
    String eventSource,
    @Schema(description = "操作者业务唯一标识")
    String operatorBizId,
    @Schema(description = "脱敏后的事件上下文 JSON")
    String eventPayload,
    @Schema(description = "事件发生时间（北京时间）")
    LocalDateTime occurredAt,
    @Schema(description = "记录创建时间（北京时间）")
    LocalDateTime createdAt
) {
}
