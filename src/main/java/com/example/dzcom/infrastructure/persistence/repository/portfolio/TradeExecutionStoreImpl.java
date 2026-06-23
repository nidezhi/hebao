package com.example.dzcom.infrastructure.persistence.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.TradeExecution;
import com.example.dzcom.domain.repository.portfolio.TradeExecutionStore;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.TradeExecutionEntity;
import com.example.dzcom.infrastructure.persistence.mapper.portfolio.TradeExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** 模拟成交仓储实现。 */
@Repository
@RequiredArgsConstructor
public class TradeExecutionStoreImpl implements TradeExecutionStore {
    private final TradeExecutionMapper mapper;

    /**
     * 保存模拟成交。
     *
     * @param execution 模拟成交领域对象
     * @return 保存后的模拟成交
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public TradeExecution save(TradeExecution execution) {
        TradeExecutionEntity entity = toEntity(execution);
        mapper.insert(entity);
        return toDomain(entity);
    }

    /**
     * 查询订单对应的第一笔成交。
     *
     * @param orderBizId 订单业务唯一标识
     * @return 模拟成交
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Optional<TradeExecution> findFirstByOrderBizId(String orderBizId) {
        return Optional.ofNullable(mapper.selectFirstByOrderBizId(orderBizId))
            .map(this::toDomain);
    }

    /** 将领域对象转换为持久化实体。 */
    private TradeExecutionEntity toEntity(TradeExecution execution) {
        return TradeExecutionEntity.builder()
            .bizId(execution.bizId())
            .executionNo(execution.executionNo())
            .orderBizId(execution.orderBizId())
            .userBizId(execution.userBizId())
            .portfolioBizId(execution.portfolioBizId())
            .productBizId(execution.productBizId())
            .channelCode(execution.channelCode())
            .externalExecutionId(execution.externalExecutionId())
            .executionPrice(execution.executionPrice())
            .executionQuantity(execution.executionQuantity())
            .executionAmount(execution.executionAmount())
            .feeAmount(execution.feeAmount())
            .executedAt(execution.executedAt())
            .createdAt(execution.createdAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private TradeExecution toDomain(TradeExecutionEntity entity) {
        return TradeExecution.builder()
            .bizId(entity.getBizId())
            .executionNo(entity.getExecutionNo())
            .orderBizId(entity.getOrderBizId())
            .userBizId(entity.getUserBizId())
            .portfolioBizId(entity.getPortfolioBizId())
            .productBizId(entity.getProductBizId())
            .channelCode(entity.getChannelCode())
            .externalExecutionId(entity.getExternalExecutionId())
            .executionPrice(entity.getExecutionPrice())
            .executionQuantity(entity.getExecutionQuantity())
            .executionAmount(entity.getExecutionAmount())
            .feeAmount(entity.getFeeAmount())
            .executedAt(entity.getExecutedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
