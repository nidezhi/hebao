package com.example.dzcom.domain.repository.ai;

/** AI Prompt 评估分页筛选条件。 */
public record AiPromptEvaluationSearchCriteria(
    String visibleUserBizId,
    String promptCode,
    String promptVersion,
    String scenario,
    String backtestBizId,
    String feedbackBizId,
    String reviewStatus,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
