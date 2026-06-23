package com.example.dzcom.infrastructure.persistence.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.repository.portfolio.PortfolioValuationStore;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.PortfolioValuationEntity;
import com.example.dzcom.infrastructure.persistence.mapper.portfolio.PortfolioValuationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 模拟组合估值快照仓储实现。 */
@Repository
@RequiredArgsConstructor
public class PortfolioValuationStoreImpl implements PortfolioValuationStore {
    private final PortfolioValuationMapper mapper;

    /**
     * 保存组合估值快照。
     *
     * @param valuation 组合估值快照
     * @return 保存后的估值快照
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public PortfolioValuation save(PortfolioValuation valuation) {
        PortfolioValuationEntity entity = toEntity(valuation);
        mapper.insert(entity);
        return toDomain(entity);
    }

    /**
     * 查询组合最新估值快照。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 最新估值快照
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Optional<PortfolioValuation> findLatestByPortfolioBizId(String portfolioBizId) {
        return Optional.ofNullable(mapper.selectLatestByPortfolioBizId(portfolioBizId))
            .map(this::toDomain);
    }

    /**
     * 查询组合首个估值快照。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 首个估值快照
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Optional<PortfolioValuation> findFirstByPortfolioBizId(String portfolioBizId) {
        return Optional.ofNullable(mapper.selectFirstByPortfolioBizId(portfolioBizId))
            .map(this::toDomain);
    }

    /**
     * 查询组合估值曲线。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param limit 返回数量上限
     * @return 估值快照集合
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public List<PortfolioValuation> findHistoryByPortfolioBizId(String portfolioBizId, int limit) {
        return mapper.selectHistoryByPortfolioBizId(portfolioBizId, limit).stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 将估值领域对象转换为持久化实体。
     *
     * @param valuation 估值领域对象
     * @return 估值持久化实体
     * @author dz
     * @date 2026-06-23
     */
    private PortfolioValuationEntity toEntity(PortfolioValuation valuation) {
        return PortfolioValuationEntity.builder()
            .bizId(valuation.bizId())
            .portfolioBizId(valuation.portfolioBizId())
            .valuationTime(valuation.valuationTime())
            .baseCurrency(valuation.baseCurrency())
            .totalAsset(valuation.totalAsset())
            .cashBalance(valuation.cashBalance())
            .positionValue(valuation.positionValue())
            .totalCost(valuation.totalCost())
            .unrealizedProfit(valuation.unrealizedProfit())
            .realizedProfit(valuation.realizedProfit())
            .totalReturnRate(valuation.totalReturnRate())
            .sourceCode(valuation.sourceCode())
            .createdAt(valuation.createdAt())
            .build();
    }

    /**
     * 将估值实体转换为领域对象。
     *
     * @param entity 估值持久化实体
     * @return 估值领域对象
     * @author dz
     * @date 2026-06-23
     */
    private PortfolioValuation toDomain(PortfolioValuationEntity entity) {
        return PortfolioValuation.builder()
            .bizId(entity.getBizId())
            .portfolioBizId(entity.getPortfolioBizId())
            .valuationTime(entity.getValuationTime())
            .baseCurrency(entity.getBaseCurrency())
            .totalAsset(entity.getTotalAsset())
            .cashBalance(entity.getCashBalance())
            .positionValue(entity.getPositionValue())
            .totalCost(entity.getTotalCost())
            .unrealizedProfit(entity.getUnrealizedProfit())
            .realizedProfit(entity.getRealizedProfit())
            .totalReturnRate(entity.getTotalReturnRate())
            .sourceCode(entity.getSourceCode())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
