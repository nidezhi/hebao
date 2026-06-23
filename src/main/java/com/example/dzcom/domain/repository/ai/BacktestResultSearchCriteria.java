package com.example.dzcom.domain.repository.ai;

/** 回测结果分页筛选条件。 */
public record BacktestResultSearchCriteria(
    String ownerUserBizId,
    String strategyCode,
    String strategyVersion,
    String status,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
