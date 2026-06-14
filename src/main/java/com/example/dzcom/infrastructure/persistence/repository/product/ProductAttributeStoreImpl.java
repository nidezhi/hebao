package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.domain.model.product.ProductAttribute;
import com.example.dzcom.domain.repository.product.ProductAttributeStore;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductAttributeEntity;
import com.example.dzcom.infrastructure.persistence.mapper.product.ProductAttributeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品属性仓储实现，直接维护产品扩展属性数据。
 */
@Repository
@RequiredArgsConstructor
public class ProductAttributeStoreImpl implements ProductAttributeStore {
    /** MyBatis 产品属性执行器。 */
    private final ProductAttributeMapper mapper;

    /**
     * 保存产品属性。
     *
     * @param value 产品属性领域对象
     * @return 保存后的产品属性
     */
    @Override
    public ProductAttribute save(ProductAttribute value) {
        ProductAttributeEntity existing = mapper.selectById(value.bizId());
        ProductAttributeEntity entity = Optional.ofNullable(existing)
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
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 根据产品、属性键和生效日期查询属性。
     *
     * @param productBizId 产品业务标识
     * @param key 属性键
     * @param effectiveDate 生效日期
     * @param includeDeleted 是否包含已删除数据
     * @return 产品属性
     */
    @Override
    public Optional<ProductAttribute> find(String productBizId, String key, LocalDate effectiveDate,
                                           boolean includeDeleted) {
        return Optional.ofNullable(mapper.selectOne(productBizId, key, effectiveDate, includeDeleted))
            .map(this::toDomain);
    }

    /**
     * 查询产品全部未删除属性。
     *
     * @param productBizId 产品业务标识
     * @return 产品属性列表
     */
    @Override
    public List<ProductAttribute> findByProductBizId(String productBizId) {
        return mapper.selectByProductBizId(productBizId)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 将产品属性实体转换为领域对象。
     *
     * @param entity 产品属性实体
     * @return 产品属性领域对象
     */
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
