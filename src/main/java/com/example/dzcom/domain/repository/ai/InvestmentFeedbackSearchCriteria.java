package com.example.dzcom.domain.repository.ai;

/** 投资反馈分页筛选条件。 */
public record InvestmentFeedbackSearchCriteria(
    String userBizId,
    String targetType,
    String targetBizId,
    String reportBizId,
    String promptCode,
    String promptVersion,
    String backtestBizId,
    String feedbackAction,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
