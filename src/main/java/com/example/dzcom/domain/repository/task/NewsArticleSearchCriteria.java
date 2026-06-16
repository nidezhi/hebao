package com.example.dzcom.domain.repository.task;

import java.time.LocalDateTime;

/** 投资资讯分页筛选条件。 */
public record NewsArticleSearchCriteria(
    String keyword,
    String articleType,
    String sourceCode,
    String languageCode,
    LocalDateTime publishFrom,
    LocalDateTime publishTo,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
