package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.domain.model.product.ProductInvestmentProfile;
import com.example.dzcom.domain.repository.product.ProductInvestmentProfileStore;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductInvestmentProfileEntity;
import com.example.dzcom.infrastructure.persistence.mapper.product.ProductInvestmentProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** 产品投资画像仓储实现。 */
@Repository
@RequiredArgsConstructor
public class ProductInvestmentProfileStoreImpl implements ProductInvestmentProfileStore {
    private final ProductInvestmentProfileMapper mapper;

    /**
     * 保存产品投资画像。
     *
     * @param profile 产品投资风险和交易画像
     * @return 保存后的产品投资画像
     * @author dz
     * @date 2026-06-22
     */
    @Override
    public ProductInvestmentProfile save(ProductInvestmentProfile profile) {
        mapper.save(toEntity(profile));
        return profile;
    }

    /**
     * 根据产品业务标识查询投资画像。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品投资画像；不存在时返回空
     * @author dz
     * @date 2026-06-22
     */
    @Override
    public Optional<ProductInvestmentProfile> findByProductBizId(String productBizId) {
        return Optional.ofNullable(mapper.selectByProductBizId(productBizId))
            .map(this::toDomain);
    }

    /**
     * 将领域对象转换为持久化实体。
     *
     * @param profile 产品投资画像领域对象
     * @return 产品投资画像持久化实体
     * @author dz
     * @date 2026-06-22
     */
    private ProductInvestmentProfileEntity toEntity(ProductInvestmentProfile profile) {
        return ProductInvestmentProfileEntity.builder()
            .bizId(profile.bizId())
            .productBizId(profile.productBizId())
            .assetClass(profile.assetClass())
            .riskSummary(profile.riskSummary())
            .volatilityLevel(profile.volatilityLevel())
            .liquidityLevel(profile.liquidityLevel())
            .maxDrawdown(profile.maxDrawdown())
            .suitableRiskLevel(profile.suitableRiskLevel())
            .mockTradable(profile.mockTradable())
            .minHoldingDays(profile.minHoldingDays())
            .tradingNotes(profile.tradingNotes())
            .dataQualityScore(profile.dataQualityScore())
            .createdAt(profile.createdAt())
            .updatedAt(profile.updatedAt())
            .build();
    }

    /**
     * 将持久化实体转换为领域对象。
     *
     * @param entity 产品投资画像持久化实体
     * @return 产品投资画像领域对象
     * @author dz
     * @date 2026-06-22
     */
    private ProductInvestmentProfile toDomain(ProductInvestmentProfileEntity entity) {
        return ProductInvestmentProfile.builder()
            .bizId(entity.getBizId())
            .productBizId(entity.getProductBizId())
            .assetClass(entity.getAssetClass())
            .riskSummary(entity.getRiskSummary())
            .volatilityLevel(entity.getVolatilityLevel())
            .liquidityLevel(entity.getLiquidityLevel())
            .maxDrawdown(entity.getMaxDrawdown())
            .suitableRiskLevel(entity.getSuitableRiskLevel())
            .mockTradable(entity.isMockTradable())
            .minHoldingDays(entity.getMinHoldingDays())
            .tradingNotes(entity.getTradingNotes())
            .dataQualityScore(entity.getDataQualityScore())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
