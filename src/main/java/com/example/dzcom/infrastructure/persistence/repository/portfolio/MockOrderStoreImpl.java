package com.example.dzcom.infrastructure.persistence.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.MockOrder;
import com.example.dzcom.domain.repository.portfolio.MockOrderStore;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.MockOrderEntity;
import com.example.dzcom.infrastructure.persistence.mapper.portfolio.MockOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** 模拟订单仓储实现。 */
@Repository
@RequiredArgsConstructor
public class MockOrderStoreImpl implements MockOrderStore {
    private final MockOrderMapper mapper;

    /**
     * 保存模拟订单。
     *
     * @param order 模拟订单领域对象
     * @return 保存后的模拟订单
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public MockOrder save(MockOrder order) {
        MockOrderEntity entity = toEntity(order);
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 按用户和幂等键查询订单。
     *
     * @param userBizId 用户业务唯一标识
     * @param idempotencyKey 客户端幂等键
     * @return 已存在的模拟订单
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Optional<MockOrder> findByUserAndIdempotencyKey(String userBizId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.selectByUserAndIdempotencyKey(userBizId, idempotencyKey))
            .map(this::toDomain);
    }

    /** 将领域对象转换为持久化实体。 */
    private MockOrderEntity toEntity(MockOrder order) {
        return MockOrderEntity.builder()
            .bizId(order.bizId())
            .orderNo(order.orderNo())
            .idempotencyKey(order.idempotencyKey())
            .userBizId(order.userBizId())
            .portfolioBizId(order.portfolioBizId())
            .productBizId(order.productBizId())
            .channelCode(order.channelCode())
            .orderSide(order.orderSide())
            .orderType(order.orderType())
            .currency(order.currency())
            .requestedPrice(order.requestedPrice())
            .requestedQuantity(order.requestedQuantity())
            .requestedAmount(order.requestedAmount())
            .executedQuantity(order.executedQuantity())
            .executedAmount(order.executedAmount())
            .feeAmount(order.feeAmount())
            .status(order.status())
            .externalOrderId(order.externalOrderId())
            .rejectCode(order.rejectCode())
            .rejectMessage(order.rejectMessage())
            .submittedAt(order.submittedAt())
            .completedAt(order.completedAt())
            .version(order.version())
            .createdAt(order.createdAt())
            .updatedAt(order.updatedAt())
            .createdBy(order.createdBy())
            .deleted(order.deleted())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private MockOrder toDomain(MockOrderEntity entity) {
        return MockOrder.builder()
            .bizId(entity.getBizId())
            .orderNo(entity.getOrderNo())
            .idempotencyKey(entity.getIdempotencyKey())
            .userBizId(entity.getUserBizId())
            .portfolioBizId(entity.getPortfolioBizId())
            .productBizId(entity.getProductBizId())
            .channelCode(entity.getChannelCode())
            .orderSide(entity.getOrderSide())
            .orderType(entity.getOrderType())
            .currency(entity.getCurrency())
            .requestedPrice(entity.getRequestedPrice())
            .requestedQuantity(entity.getRequestedQuantity())
            .requestedAmount(entity.getRequestedAmount())
            .executedQuantity(entity.getExecutedQuantity())
            .executedAmount(entity.getExecutedAmount())
            .feeAmount(entity.getFeeAmount())
            .status(entity.getStatus())
            .externalOrderId(entity.getExternalOrderId())
            .rejectCode(entity.getRejectCode())
            .rejectMessage(entity.getRejectMessage())
            .submittedAt(entity.getSubmittedAt())
            .completedAt(entity.getCompletedAt())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .deleted(entity.getDeleted())
            .build();
    }
}
