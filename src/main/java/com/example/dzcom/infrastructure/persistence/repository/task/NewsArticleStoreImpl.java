package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.example.dzcom.infrastructure.persistence.entity.task.NewsArticleEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.NewsArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 投资资讯仓储实现。 */
@Repository
@RequiredArgsConstructor
public class NewsArticleStoreImpl implements NewsArticleStore {
    private final NewsArticleMapper mapper;

    /** 保存资讯并返回最终领域对象。 */
    @Override
    public NewsArticle save(NewsArticle article) {
        Optional<NewsArticleEntity> existing = Optional.ofNullable(
            mapper.selectBySourceAndExternalId(article.sourceCode(), article.externalId()));
        NewsArticleEntity entity = NewsArticleEntity.builder()
            .bizId(existing.map(NewsArticleEntity::getBizId).orElse(article.bizId()))
            .externalId(article.externalId())
            .articleType(article.articleType())
            .title(article.title())
            .summary(article.summary())
            .content(article.content())
            .sourceCode(article.sourceCode())
            .sourceUrl(article.sourceUrl())
            .languageCode(article.languageCode())
            .publishTime(article.publishTime())
            .collectedAt(article.collectedAt())
            .createdAt(existing.map(NewsArticleEntity::getCreatedAt).orElse(article.createdAt()))
            .deleted(0)
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /** 统计窗口内关键词命中数量。 */
    @Override
    public long countByKeywords(List<String> keywords, LocalDateTime from) {
        return mapper.countByKeywords(keywords, from);
    }

    /** 将持久化实体转换为领域对象。 */
    private NewsArticle toDomain(NewsArticleEntity entity) {
        return NewsArticle.builder()
            .bizId(entity.getBizId())
            .externalId(entity.getExternalId())
            .articleType(entity.getArticleType())
            .title(entity.getTitle())
            .summary(entity.getSummary())
            .content(entity.getContent())
            .sourceCode(entity.getSourceCode())
            .sourceUrl(entity.getSourceUrl())
            .languageCode(entity.getLanguageCode())
            .publishTime(entity.getPublishTime())
            .collectedAt(entity.getCollectedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
