package com.example.dzcom.domain.repository.task;

/** 资讯主题产品关联分页查询条件。 */
public record NewsArticleRelationSearchCriteria(
    String articleBizId,
    String themeCode,
    String productCode,
    String relationType,
    Integer page,
    Integer size,
    String sort,
    boolean ascending
) {
}
