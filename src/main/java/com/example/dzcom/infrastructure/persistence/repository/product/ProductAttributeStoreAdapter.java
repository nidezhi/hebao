package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.domain.model.product.ProductAttribute;
import com.example.dzcom.domain.repository.product.ProductAttributeStore;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductAttributeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductAttributeStoreAdapter implements ProductAttributeStore {
    private final JpaProductAttributeRepository repository;

    @Override
    public ProductAttribute save(ProductAttribute value) {
        ProductAttributeEntity entity = repository.findById(value.bizId())
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
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<ProductAttribute> find(String productBizId, String key, LocalDate effectiveDate,
                                           boolean includeDeleted) {
        Optional<ProductAttributeEntity> result = includeDeleted
            ? repository.findByProductBizIdAndAttributeKeyAndEffectiveDate(productBizId, key, effectiveDate)
            : repository.findByProductBizIdAndAttributeKeyAndEffectiveDateAndDeleted(
                productBizId, key, effectiveDate, 0);
        return result.map(this::toDomain);
    }

    @Override
    public List<ProductAttribute> findByProductBizId(String productBizId) {
        return repository.findAllByProductBizIdAndDeletedOrderByAttributeKeyAscEffectiveDateDesc(
                productBizId, 0).stream()
            .map(this::toDomain)
            .toList();
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
}
