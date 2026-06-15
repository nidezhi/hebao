package com.example.dzcom.domain.model.task;

import lombok.Builder;

import java.time.LocalDateTime;

/** 投资资讯领域对象。 */
@Builder
public record NewsArticle(
    String bizId,
    String externalId,
    String articleType,
    String title,
    String summary,
    String content,
    String sourceCode,
    String sourceUrl,
    String languageCode,
    LocalDateTime publishTime,
    LocalDateTime collectedAt,
    LocalDateTime createdAt
) {
}
