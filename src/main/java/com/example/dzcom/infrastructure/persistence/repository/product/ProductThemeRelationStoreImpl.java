package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.domain.model.product.ProductThemeRelation;
import com.example.dzcom.domain.repository.product.ProductThemeRelationStore;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductThemeRelationEntity;
import com.example.dzcom.infrastructure.persistence.mapper.product.ProductThemeRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 产品主题关系仓储实现。 */
@Repository
@RequiredArgsConstructor
public class ProductThemeRelationStoreImpl implements ProductThemeRelationStore {
    private final ProductThemeRelationMapper mapper;

    /**
     * 替换指定产品的全部主题关系。
     *
     * @param productBizId 产品业务唯一标识
     * @param relations 新的主题关系集合
     * @author dz
     * @date 2026-06-22
     */
    @Override
    public void replaceByProductBizId(String productBizId, List<ProductThemeRelation> relations) {
        mapper.deleteByProductBizId(productBizId);
        if (relations == null || relations.isEmpty()) {
            return;
        }
        mapper.insertBatch(relations.stream()
            .map(this::toEntity)
            .toList());
    }

    /**
     * 根据产品业务标识查询全部主题关系。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品主题关系集合
     * @author dz
     * @date 2026-06-22
     */
    @Override
    public List<ProductThemeRelation> findByProductBizId(String productBizId) {
        return mapper.selectByProductBizId(productBizId).stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 将领域对象转换为持久化实体。
     *
     * @param relation 产品主题关系领域对象
     * @return 产品主题关系持久化实体
     * @author dz
     * @date 2026-06-22
     */
    private ProductThemeRelationEntity toEntity(ProductThemeRelation relation) {
        return ProductThemeRelationEntity.builder()
            .bizId(relation.bizId())
            .productBizId(relation.productBizId())
            .relationType(relation.relationType())
            .relationCode(relation.relationCode())
            .relationName(relation.relationName())
            .relationWeight(relation.relationWeight())
            .sourceCode(relation.sourceCode())
            .evidence(relation.evidence())
            .createdAt(relation.createdAt())
            .updatedAt(relation.updatedAt())
            .build();
    }

    /**
     * 将持久化实体转换为领域对象。
     *
     * @param entity 产品主题关系持久化实体
     * @return 产品主题关系领域对象
     * @author dz
     * @date 2026-06-22
     */
    private ProductThemeRelation toDomain(ProductThemeRelationEntity entity) {
        return ProductThemeRelation.builder()
            .bizId(entity.getBizId())
            .productBizId(entity.getProductBizId())
            .relationType(entity.getRelationType())
            .relationCode(entity.getRelationCode())
            .relationName(entity.getRelationName())
            .relationWeight(entity.getRelationWeight())
            .sourceCode(entity.getSourceCode())
            .evidence(entity.getEvidence())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
