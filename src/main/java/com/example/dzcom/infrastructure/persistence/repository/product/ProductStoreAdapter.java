package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductAttribute;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductAttributeEntity;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 产品仓储 JPA 适配器。
 *
 * <p>负责领域对象与 ORM 实体的显式转换，并集中处理软删除过滤。
 * 应用层不感知 Spring Data 的 Page、Sort 或派生查询命名。</p>
 */
@Repository
@RequiredArgsConstructor
public class ProductStoreAdapter implements ProductStore {
    private final JpaProductRepository products;
    private final JpaProductAttributeRepository attributes;

    @Override
    public Product save(Product value) {
        ProductEntity entity = products.findById(value.getBizId())
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
        return toDomain(products.save(entity));
    }

    @Override
    public Optional<Product> findByBizId(String bizId) {
        return products.findByBizIdAndDeleted(bizId, 0).map(this::toDomain);
    }

    @Override
    public boolean existsByMarketAndCode(String marketCode, String productCode) {
        return products.existsByMarketCodeAndProductCodeAndDeleted(marketCode, productCode, 0);
    }

    @Override
    public PageResult<Product> search(ProductSearchCriteria criteria) {
        Sort sort = Sort.by(criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, criteria.sort());
        PageRequest pageable = PageRequest.of(criteria.page() - 1, criteria.size(), sort);
        Page<ProductEntity> page = products.search(
            blankToNull(criteria.keyword()),
            criteria.productType() == null ? null : criteria.productType().name(),
            criteria.tradeStatus() == null ? null : criteria.tradeStatus().code(),
            criteria.riskLevel(),
            criteria.currency() == null ? null : criteria.currency().trim().toUpperCase(Locale.ROOT),
            pageable
        );
        return PageResult.<Product>builder()
            .items(page.getContent().stream().map(this::toDomain).toList())
            .total(page.getTotalElements())
            .page(criteria.page())
            .size(criteria.size())
            .totalPages(page.getTotalPages())
            .build();
    }

    @Override
    public ProductAttribute saveAttribute(ProductAttribute value) {
        ProductAttributeEntity entity = attributes.findById(value.bizId())
            .map(ProductAttributeEntity::toBuilder)
            .orElseGet(ProductAttributeEntity::builder)
            .bizId(value.bizId())
            .productBizId(value.productBizId())
            .attributeKey(value.key())
            .valueType(value.valueType())
            .attributeValue(value.jsonValue())
            .effectiveDate(value.effectiveDate())
            .sourceCode(value.sourceCode())
            .createdAt(value.createdAt())
            .updatedAt(value.updatedAt())
            .deleted(value.deleted())
            .build();
        return toDomain(attributes.save(entity));
    }

    @Override
    public Optional<ProductAttribute> findAttribute(String productBizId, String key,
                                                    LocalDate effectiveDate, boolean includeDeleted) {
        Optional<ProductAttributeEntity> result = includeDeleted
            ? attributes.findByProductBizIdAndAttributeKeyAndEffectiveDate(productBizId, key, effectiveDate)
            : attributes.findByProductBizIdAndAttributeKeyAndEffectiveDateAndDeleted(
                productBizId, key, effectiveDate, 0);
        return result.map(this::toDomain);
    }

    @Override
    public List<ProductAttribute> findAttributes(String productBizId) {
        return attributes.findAllByProductBizIdAndDeletedOrderByAttributeKeyAscEffectiveDateDesc(productBizId, 0)
            .stream().map(this::toDomain).toList();
    }

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

    private ProductAttribute toDomain(ProductAttributeEntity entity) {
        return ProductAttribute.builder()
            .bizId(entity.getBizId())
            .productBizId(entity.getProductBizId())
            .key(entity.getAttributeKey())
            .valueType(entity.getValueType())
            .jsonValue(entity.getAttributeValue())
            .effectiveDate(entity.getEffectiveDate())
            .sourceCode(entity.getSourceCode())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deleted(entity.getDeleted())
            .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
