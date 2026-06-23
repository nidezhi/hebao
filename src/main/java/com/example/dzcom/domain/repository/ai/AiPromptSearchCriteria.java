package com.example.dzcom.domain.repository.ai;

/** AI Prompt 模板分页筛选条件。 */
public record AiPromptSearchCriteria(
    String promptCode,
    String scenario,
    String status,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
