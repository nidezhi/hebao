package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.domain.model.task.NewsArticleRelation;
import com.example.dzcom.domain.repository.task.NewsArticleRelationStore;
import com.example.dzcom.infrastructure.persistence.entity.task.NewsArticleRelationEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.NewsArticleRelationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 资讯主题产品关联仓储实现。 */
@Repository
@RequiredArgsConstructor
public class NewsArticleRelationStoreImpl implements NewsArticleRelationStore {
    private final NewsArticleRelationMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * 批量保存资讯、主题和产品的显式关联。
     *
     * @param relations 待保存的领域关联集合
     * @author dz
     * @date 2026-06-21
     */
    @Override
    public void saveBatch(List<NewsArticleRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return;
        }
        List<NewsArticleRelationEntity> entities = relations.stream()
            .map(this::toEntity)
            .toList();
        mapper.saveBatch(entities);
    }

    /**
     * 将领域关联对象转换为持久化实体。
     *
     * @param relation 资讯主题产品关联领域对象
     * @return 持久化实体
     * @author dz
     * @date 2026-06-21
     */
    private NewsArticleRelationEntity toEntity(NewsArticleRelation relation) {
        return NewsArticleRelationEntity.builder()
            .bizId(relation.bizId())
            .articleBizId(relation.articleBizId())
            .themeCode(relation.themeCode())
            .themeName(relation.themeName())
            .productCode(relation.productCode() == null ? "" : relation.productCode())
            .relationType(relation.relationType())
            .matchedKeywords(writeKeywords(relation))
            .sourceQualityScore(relation.sourceQualityScore())
            .relationScore(relation.relationScore())
            .evidence(relation.evidence())
            .createdAt(relation.createdAt())
            .build();
    }

    /**
     * 序列化命中关键词集合。
     *
     * @param relation 资讯主题产品关联领域对象
     * @return JSON 字符串
     * @throws IllegalStateException 当序列化失败时抛出
     * @author dz
     * @date 2026-06-21
     */
    private String writeKeywords(NewsArticleRelation relation) {
        try {
            return objectMapper.writeValueAsString(relation.matchedKeywords());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("资讯关联关键词序列化失败", exception);
        }
    }
}
