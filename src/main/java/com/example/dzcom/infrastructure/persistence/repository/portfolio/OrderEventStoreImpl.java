package com.example.dzcom.infrastructure.persistence.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.OrderEvent;
import com.example.dzcom.domain.repository.portfolio.OrderEventStore;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.OrderEventEntity;
import com.example.dzcom.infrastructure.persistence.mapper.portfolio.OrderEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 模拟订单事件仓储实现。 */
@Repository
@RequiredArgsConstructor
public class OrderEventStoreImpl implements OrderEventStore {
    private final OrderEventMapper mapper;

    /**
     * 追加保存订单状态事件。
     *
     * @param event 订单状态事件
     * @return 保存后的订单状态事件
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public OrderEvent save(OrderEvent event) {
        OrderEventEntity entity = toEntity(event);
        mapper.insert(entity);
        return toDomain(entity);
    }

    /**
     * 查询订单生命周期事件。
     *
     * @param orderBizId 订单业务唯一标识
     * @return 按发生时间升序排列的订单事件
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public List<OrderEvent> findByOrderBizId(String orderBizId) {
        return mapper.selectByOrderBizId(orderBizId).stream()
            .map(this::toDomain)
            .toList();
    }

    /** 将领域对象转换为持久化实体。 */
    private OrderEventEntity toEntity(OrderEvent event) {
        return OrderEventEntity.builder()
            .bizId(event.bizId())
            .orderBizId(event.orderBizId())
            .eventType(event.eventType())
            .fromStatus(event.fromStatus())
            .toStatus(event.toStatus())
            .eventSource(event.eventSource())
            .operatorBizId(event.operatorBizId())
            .eventPayload(event.eventPayload())
            .occurredAt(event.occurredAt())
            .createdAt(event.createdAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private OrderEvent toDomain(OrderEventEntity entity) {
        return OrderEvent.builder()
            .bizId(entity.getBizId())
            .orderBizId(entity.getOrderBizId())
            .eventType(entity.getEventType())
            .fromStatus(entity.getFromStatus())
            .toStatus(entity.getToStatus())
            .eventSource(entity.getEventSource())
            .operatorBizId(entity.getOperatorBizId())
            .eventPayload(entity.getEventPayload())
            .occurredAt(entity.getOccurredAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
