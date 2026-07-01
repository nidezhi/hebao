package com.example.dzcom.domain.repository.ai;

/** AI 模型调用审计筛选条件。 */
public record AiModelCallAuditSearchCriteria(
    String operationCode,
    String callStatus,
    String providerCode,
    String modelCode,
    String modelVersion,
    String businessType,
    String businessBizId,
    String taskCode,
    String eventId,
    String runBizId,
    String runNo,
    String reportBizId,
    String promptCode,
    String skillCode,
    String scenarioCode,
    String environment,
    int page,
    int size,
    String sort,
    boolean asc
) {
}
