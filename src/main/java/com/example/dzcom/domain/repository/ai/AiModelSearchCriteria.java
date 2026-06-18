package com.example.dzcom.domain.repository.ai;

/** AI 模型分页筛选条件。 */
public record AiModelSearchCriteria(
    String modelCode,
    String modelType,
    String provider,
    String status,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
