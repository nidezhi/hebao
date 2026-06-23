package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 模拟订单事件应用层视图。 */
@Builder
@Schema(description = "模拟订单事件应用层视图")
public record OrderEventView(
    @Schema(description = "订单事件业务唯一标识")
    String bizId,
    @Schema(description = "订单业务唯一标识")
    String orderBizId,
    @Schema(description = "事件类型")
    String eventType,
    @Schema(description = "变更前订单状态")
    String fromStatus,
    @Schema(description = "变更后订单状态")
    String toStatus,
    @Schema(description = "事件来源")
    String eventSource,
    @Schema(description = "操作者业务唯一标识")
    String operatorBizId,
    @Schema(description = "脱敏后的事件上下文 JSON")
    String eventPayload,
    @Schema(description = "事件发生时间（北京时间）")
    LocalDateTime occurredAt
) {
}
