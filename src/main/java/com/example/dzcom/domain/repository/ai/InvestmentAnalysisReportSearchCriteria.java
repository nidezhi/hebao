package com.example.dzcom.domain.repository.ai;

/** 投资分析报告分页筛选条件。 */
public record InvestmentAnalysisReportSearchCriteria(
    String marketScope,
    String themeCode,
    String providerCode,
    String status,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
