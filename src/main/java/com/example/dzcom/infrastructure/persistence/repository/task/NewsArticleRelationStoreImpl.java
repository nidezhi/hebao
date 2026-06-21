package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.NewsArticleRelation;
import com.example.dzcom.domain.repository.task.NewsArticleRelationSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleRelationStore;
import com.example.dzcom.infrastructure.persistence.entity.task.NewsArticleRelationEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.NewsArticleRelationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/** 资讯主题产品关联仓储实现。 */
@Repository
@RequiredArgsConstructor
public class NewsArticleRelationStoreImpl implements NewsArticleRelationStore {
    private static final Set<String> SORTS = Set.of(
        "createdAt",
        "relationScore",
        "sourceQualityScore",
        "themeCode",
        "productCode"
    );

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
     * 根据筛选条件分页查询资讯、主题和产品关联。
     *
     * @param criteria 资讯、主题、产品和分页排序筛选条件
     * @return 资讯主题产品关联分页结果
     * @author dz
     * @date 2026-06-21
     */
    @Override
    public PageResult<NewsArticleRelation> search(NewsArticleRelationSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<NewsArticleRelation> items = mapper.search(
                criteria,
                offset,
                resolveSortColumn(criteria.sort())
            )
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<NewsArticleRelation>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
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
     * 将持久化实体转换为领域对象。
     *
     * @param entity 资讯主题产品关联持久化实体
     * @return 领域关联对象
     * @author dz
     * @date 2026-06-21
     */
    private NewsArticleRelation toDomain(NewsArticleRelationEntity entity) {
        return NewsArticleRelation.builder()
            .bizId(entity.getBizId())
            .articleBizId(entity.getArticleBizId())
            .themeCode(entity.getThemeCode())
            .themeName(entity.getThemeName())
            .productCode(entity.getProductCode())
            .relationType(entity.getRelationType())
            .matchedKeywords(readKeywords(entity))
            .sourceQualityScore(entity.getSourceQualityScore())
            .relationScore(entity.getRelationScore())
            .evidence(entity.getEvidence())
            .createdAt(entity.getCreatedAt())
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

    /**
     * 读取持久化的命中关键词 JSON。
     *
     * @param entity 资讯主题产品关联持久化实体
     * @return 命中关键词集合
     * @author dz
     * @date 2026-06-21
     */
    private List<String> readKeywords(NewsArticleRelationEntity entity) {
        if (entity.getMatchedKeywords() == null || entity.getMatchedKeywords().isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                entity.getMatchedKeywords(),
                new TypeReference<List<String>>() {
                }
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("资讯关联关键词反序列化失败", exception);
        }
    }

    /**
     * 将接口排序字段转换为固定数据库列。
     *
     * @param sort 接口层排序字段
     * @return 数据库排序列
     * @author dz
     * @date 2026-06-21
     */
    private String resolveSortColumn(String sort) {
        String safeSort = SORTS.contains(sort) ? sort : "createdAt";
        return switch (safeSort) {
            case "relationScore" -> "r.relation_score";
            case "sourceQualityScore" -> "r.source_quality_score";
            case "themeCode" -> "r.theme_code";
            case "productCode" -> "r.product_code";
            default -> "r.created_at";
        };
    }
}
