package com.example.dzcom.domain.repository.market;

/** 数据源分页筛选条件。 */
public record DataSourceSearchCriteria(
    String keyword,
    String sourceType,
    String trustLevel,
    Boolean enabled,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
