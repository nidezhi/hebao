package com.example.dzcom.infrastructure.persistence.entity.portfolio;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 模拟订单事件持久化实体。 */
@Schema(description = "模拟订单事件持久化实体")
@TableName("aiw_order_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderEventEntity {
    /** 订单事件业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "订单事件业务唯一标识")
    private String bizId;
    /** 订单业务唯一标识。 */
    @Schema(description = "订单业务唯一标识")
    private String orderBizId;
    /** 事件类型。 */
    @Schema(description = "事件类型")
    private String eventType;
    /** 变更前订单状态。 */
    @Schema(description = "变更前订单状态")
    private String fromStatus;
    /** 变更后订单状态。 */
    @Schema(description = "变更后订单状态")
    private String toStatus;
    /** 事件来源。 */
    @Schema(description = "事件来源")
    private String eventSource;
    /** 操作者业务唯一标识。 */
    @Schema(description = "操作者业务唯一标识")
    private String operatorBizId;
    /** 脱敏后的事件上下文 JSON。 */
    @Schema(description = "脱敏后的事件上下文 JSON")
    private String eventPayload;
    /** 事件发生时间（北京时间）。 */
    @Schema(description = "事件发生时间（北京时间）")
    private LocalDateTime occurredAt;
    /** 记录创建时间（北京时间）。 */
    @Schema(description = "记录创建时间（北京时间）")
    private LocalDateTime createdAt;
}
