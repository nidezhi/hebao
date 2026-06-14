package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductEntity;
import com.example.dzcom.infrastructure.persistence.mapper.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产品仓储实现，直接负责产品主表查询、分页、保存和领域转换。
 */
@Repository
@RequiredArgsConstructor
public class ProductStoreImpl implements ProductStore {
    /** MyBatis 产品执行器。 */
    private final ProductMapper mapper;

    /**
     * 保存产品。
     *
     * @param value 产品领域对象
     * @return 保存后的产品
     */
    @Override
    public Product save(Product value) {
        ProductEntity existing = mapper.selectById(value.getBizId());
        ProductEntity entity = Optional.ofNullable(existing)
            .map(ProductEntity::toBuilder)
            .orElseGet(ProductEntity::builder)
            .bizId(value.getBizId())
            .productNo(value.getProductNo())
            .productCode(value.getProductCode())
            .productName(value.getProductName())
            .productType(value.getProductType().name())
            .marketCode(value.getMarketCode())
            .currency(value.getCurrency())
            .tradeStatus(value.getTradeStatus().code())
            .riskLevel(value.getRiskLevel())
            .minInvestAmount(value.getMinInvestAmount())
            .amountStep(value.getAmountStep())
            .quantityStep(value.getQuantityStep())
            .feeRate(value.getFeeRate())
            .listingDate(value.getListingDate())
            .delistingDate(value.getDelistingDate())
            .description(value.getDescription())
            .version(value.getVersion())
            .createdAt(value.getCreatedAt())
            .updatedAt(value.getUpdatedAt())
            .createdBy(value.getCreatedBy())
            .updatedBy(value.getUpdatedBy())
            .deleted(value.getDeleted())
            .deletedAt(value.getDeletedAt())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 根据业务标识查询未删除产品。
     *
     * @param bizId 产品业务标识
     * @return 产品领域对象
     */
    @Override
    public Optional<Product> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectActiveByBizId(bizId))
            .map(this::toDomain);
    }

    /**
     * 判断市场内是否存在相同产品代码。
     *
     * @param marketCode 市场代码
     * @param productCode 产品代码
     * @return 存在时返回 true
     */
    @Override
    public boolean existsByMarketAndCode(String marketCode, String productCode) {
        return mapper.countByMarketAndCode(marketCode, productCode) > 0;
    }

    /**
     * 根据筛选条件分页查询产品。
     *
     * @param criteria 产品筛选和分页条件
     * @return 产品分页结果
     */
    @Override
    public PageResult<Product> search(ProductSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<Product> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<Product>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /**
     * 将接口排序字段转换为固定数据库列，避免动态 SQL 注入。
     *
     * @param sort 接口排序字段
     * @return 数据库排序列
     */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "productNo" -> "p.product_no";
            case "productCode" -> "p.product_code";
            case "productName" -> "p.product_name";
            case "riskLevel" -> "p.risk_level";
            case "listingDate" -> "p.listing_date";
            default -> "p.created_at";
        };
    }

    /**
     * 将产品实体转换为领域对象。
     *
     * @param entity 产品实体
     * @return 产品领域对象
     */
    private Product toDomain(ProductEntity entity) {
        return Product.builder()
            .bizId(entity.getBizId())
            .productNo(entity.getProductNo())
            .productCode(entity.getProductCode())
            .productName(entity.getProductName())
            .productType(ProductType.valueOf(entity.getProductType()))
            .marketCode(entity.getMarketCode())
            .currency(entity.getCurrency())
            .tradeStatus(ProductTradeStatus.fromCode(entity.getTradeStatus()))
            .riskLevel(entity.getRiskLevel())
            .minInvestAmount(entity.getMinInvestAmount())
            .amountStep(entity.getAmountStep())
            .quantityStep(entity.getQuantityStep())
            .feeRate(entity.getFeeRate())
            .listingDate(entity.getListingDate())
            .delistingDate(entity.getDelistingDate())
            .description(entity.getDescription())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .deleted(entity.getDeleted())
            .deletedAt(entity.getDeletedAt())
            .build();
    }

}
