package com.example.dzcom.application.assembler.product;

import com.example.dzcom.application.dto.product.ProductAttributeView;
import com.example.dzcom.application.dto.product.ProductInvestmentProfileView;
import com.example.dzcom.application.dto.product.ProductThemeRelationView;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductAttribute;
import com.example.dzcom.domain.model.product.ProductInvestmentProfile;
import com.example.dzcom.domain.model.product.ProductThemeRelation;
import com.example.dzcom.domain.model.market.MarketQuote;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** 将产品领域数据转换为稳定的接口视图。 */
@Component
public class ProductViewAssembler {
    /**
     * 组装列表项；刻意不读取扩展属性，避免分页查询产生 N+1。
     *
     * @param product product 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    public ProductView assembleSummary(Product product) {
        return assemble(product, List.of(), null, List.of(), Optional.empty());
    }

    /**
     * 组装带前端选择器和工作台摘要字段的产品列表项。
     *
     * @param product 产品领域对象
     * @param investmentProfile 产品投资画像
     * @param latestQuote 最新行情
     * @return 产品摘要视图
     */
    public ProductView assembleSummary(
        Product product,
        Optional<ProductInvestmentProfile> investmentProfile,
        Optional<MarketQuote> latestQuote
    ) {
        return assemble(product, List.of(), investmentProfile.map(this::toProfileView).orElse(null), List.of(), latestQuote);
    }

    /**
     * 组装包含低频扩展属性的产品详情。
     *
     * @param product product 参数
     * @param attributes attributes 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    public ProductView assembleDetail(Product product, List<ProductAttribute> attributes) {
        return assemble(product, attributes.stream().map(this::toView).toList(), null, List.of(), Optional.empty());
    }

    /**
     * 组装包含产品投资画像和主题关系的产品详情。
     *
     * @param product 产品领域对象
     * @param attributes 产品扩展属性集合
     * @param investmentProfile 产品投资风险和交易画像
     * @param themeRelations 产品主题、行业、指数和资产类别关系集合
     * @return 产品详情视图
     * @author dz
     * @date 2026-06-22
     */
    public ProductView assembleInvestmentDetail(
        Product product,
        List<ProductAttribute> attributes,
        Optional<ProductInvestmentProfile> investmentProfile,
        List<ProductThemeRelation> themeRelations
    ) {
        return assembleInvestmentDetail(product, attributes, investmentProfile, themeRelations, Optional.empty());
    }

    /**
     * 组装包含投资画像、主题关系和最新行情摘要的产品详情。
     *
     * @param product 产品领域对象
     * @param attributes 产品扩展属性集合
     * @param investmentProfile 产品投资风险和交易画像
     * @param themeRelations 产品主题、行业、指数和资产类别关系集合
     * @param latestQuote 最近 1D 行情
     * @return 产品详情视图
     */
    public ProductView assembleInvestmentDetail(
        Product product,
        List<ProductAttribute> attributes,
        Optional<ProductInvestmentProfile> investmentProfile,
        List<ProductThemeRelation> themeRelations,
        Optional<MarketQuote> latestQuote
    ) {
        ProductInvestmentProfileView profileView = investmentProfile
            .map(this::toProfileView)
            .orElse(null);
        List<ProductThemeRelationView> relationViews = themeRelations.stream()
            .map(this::toRelationView)
            .toList();
        return assemble(
            product,
            attributes.stream().map(this::toView).toList(),
            profileView,
            relationViews,
            latestQuote
        );
    }

    /**
     * 将源对象转换为目标视图或领域对象。
     *
     * @param product product 参数
     * @param attributes attributes 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    private ProductView assemble(
        Product product,
        List<ProductAttributeView> attributes,
        ProductInvestmentProfileView investmentProfile,
        List<ProductThemeRelationView> themeRelations,
        Optional<MarketQuote> latestQuote
    ) {
        BigDecimal dataQualityScore = investmentProfile == null ? null : investmentProfile.dataQualityScore();
        return ProductView.builder()
            .bizId(product.getBizId())
            .productNo(product.getProductNo())
            .productCode(product.getProductCode())
            .productName(product.getProductName())
            .productType(product.getProductType())
            .marketCode(product.getMarketCode())
            .currency(product.getCurrency())
            .tradeStatus(product.getTradeStatus())
            .riskLevel(product.getRiskLevel())
            .minInvestAmount(product.getMinInvestAmount())
            .amountStep(product.getAmountStep())
            .quantityStep(product.getQuantityStep())
            .feeRate(product.getFeeRate())
            .listingDate(product.getListingDate())
            .delistingDate(product.getDelistingDate())
            .description(product.getDescription())
            .attributes(attributes)
            .investmentProfile(investmentProfile)
            .themeRelations(themeRelations)
            .latestNav(latestQuote.map(MarketQuote::closePrice).orElse(null))
            .latestQuoteTime(latestQuote.map(MarketQuote::quoteTime).orElse(null))
            .sourceCode(latestQuote.map(MarketQuote::sourceCode).orElse(null))
            .dataQualityScore(dataQualityScore)
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }

    /**
     * 将源对象转换为目标视图或领域对象。
     *
     * @param attribute attribute 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    private ProductAttributeView toView(ProductAttribute attribute) {
        return ProductAttributeView.builder()
            .key(attribute.key())
            .valueType(attribute.valueType())
            .jsonValue(attribute.jsonValue())
            .effectiveDate(attribute.effectiveDate())
            .sourceCode(attribute.sourceCode())
            .build();
    }

    /**
     * 将产品投资画像领域对象转换为应用视图。
     *
     * @param profile 产品投资风险和交易画像
     * @return 产品投资画像视图
     * @author dz
     * @date 2026-06-22
     */
    private ProductInvestmentProfileView toProfileView(ProductInvestmentProfile profile) {
        return ProductInvestmentProfileView.builder()
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
            .build();
    }

    /**
     * 将产品主题关系领域对象转换为应用视图。
     *
     * @param relation 产品主题、行业、指数或资产类别关系
     * @return 产品主题关系视图
     * @author dz
     * @date 2026-06-22
     */
    private ProductThemeRelationView toRelationView(ProductThemeRelation relation) {
        return ProductThemeRelationView.builder()
            .relationType(relation.relationType())
            .relationCode(relation.relationCode())
            .relationName(relation.relationName())
            .relationWeight(relation.relationWeight())
            .sourceCode(relation.sourceCode())
            .evidence(relation.evidence())
            .build();
    }
}
