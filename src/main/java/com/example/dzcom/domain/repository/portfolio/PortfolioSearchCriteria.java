package com.example.dzcom.domain.repository.portfolio;

/** 模拟组合分页查询条件。 */
public record PortfolioSearchCriteria(
    String ownerUserBizId,
    String portfolioType,
    Integer status,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
