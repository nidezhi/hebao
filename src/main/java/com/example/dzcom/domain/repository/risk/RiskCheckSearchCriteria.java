package com.example.dzcom.domain.repository.risk;

/** 风险检查分页筛选条件。 */
public record RiskCheckSearchCriteria(
    String businessType,
    String businessBizId,
    String userBizId,
    String checkResult,
    String riskLevel,
    String reasonCode,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
