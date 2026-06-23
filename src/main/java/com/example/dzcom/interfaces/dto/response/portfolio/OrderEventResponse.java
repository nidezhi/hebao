package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.OrderEventView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 模拟订单事件响应。 */
@Builder
@Schema(description = "模拟订单事件响应")
public record OrderEventResponse(
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
    /**
     * 从应用层视图转换为接口响应。
     *
     * @param view 模拟订单事件应用层视图
     * @return 模拟订单事件响应
     * @author dz
     * @date 2026-06-23
     */
    public static OrderEventResponse from(OrderEventView view) {
        return OrderEventResponse.builder()
            .bizId(view.bizId())
            .orderBizId(view.orderBizId())
            .eventType(view.eventType())
            .fromStatus(view.fromStatus())
            .toStatus(view.toStatus())
            .eventSource(view.eventSource())
            .operatorBizId(view.operatorBizId())
            .eventPayload(view.eventPayload())
            .occurredAt(view.occurredAt())
            .build();
    }
}
