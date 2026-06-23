package com.example.dzcom.infrastructure.persistence.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.Position;
import com.example.dzcom.domain.repository.portfolio.PositionStore;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.PositionEntity;
import com.example.dzcom.infrastructure.persistence.mapper.portfolio.PositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 模拟组合持仓仓储实现。 */
@Repository
@RequiredArgsConstructor
public class PositionStoreImpl implements PositionStore {
    private final PositionMapper mapper;

    /**
     * 保存或覆盖当前持仓。
     *
     * @param position 当前持仓领域对象
     * @return 保存后的持仓
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Position save(Position position) {
        PositionEntity entity = toEntity(position);
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 查询指定组合、产品和方向的当前持仓。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param productBizId 产品业务唯一标识
     * @param positionSide 持仓方向
     * @return 当前持仓
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Optional<Position> findByDimension(String portfolioBizId, String productBizId, String positionSide) {
        return Optional.ofNullable(mapper.selectByDimension(portfolioBizId, productBizId, positionSide))
            .map(this::toDomain);
    }

    /**
     * 查询组合当前有效持仓。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 当前持仓集合
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public List<Position> findByPortfolioBizId(String portfolioBizId) {
        return mapper.selectByPortfolioBizId(portfolioBizId).stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 将持仓实体转换为领域对象。
     *
     * @param entity 持仓持久化实体
     * @return 持仓领域对象
     * @author dz
     * @date 2026-06-23
     */
    private Position toDomain(PositionEntity entity) {
        return Position.builder()
            .bizId(entity.getBizId())
            .portfolioBizId(entity.getPortfolioBizId())
            .productBizId(entity.getProductBizId())
            .positionSide(entity.getPositionSide())
            .quantity(entity.getQuantity())
            .availableQuantity(entity.getAvailableQuantity())
            .averageCost(entity.getAverageCost())
            .costAmount(entity.getCostAmount())
            .realizedProfit(entity.getRealizedProfit())
            .lastTradeAt(entity.getLastTradeAt())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deleted(entity.getDeleted())
            .build();
    }

    /**
     * 将持仓领域对象转换为持久化实体。
     *
     * @param position 持仓领域对象
     * @return 持仓持久化实体
     * @author dz
     * @date 2026-06-23
     */
    private PositionEntity toEntity(Position position) {
        return PositionEntity.builder()
            .bizId(position.bizId())
            .portfolioBizId(position.portfolioBizId())
            .productBizId(position.productBizId())
            .positionSide(position.positionSide())
            .quantity(position.quantity())
            .availableQuantity(position.availableQuantity())
            .averageCost(position.averageCost())
            .costAmount(position.costAmount())
            .realizedProfit(position.realizedProfit())
            .lastTradeAt(position.lastTradeAt())
            .version(position.version())
            .createdAt(position.createdAt())
            .updatedAt(position.updatedAt())
            .deleted(position.deleted())
            .build();
    }
}
