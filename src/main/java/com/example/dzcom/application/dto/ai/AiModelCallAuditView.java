package com.example.dzcom.application.dto.ai;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * AI 模型调用审计展示视图。
 *
 * <p>该视图面向前端统一模型调用中心，除保留脱敏输入输出预览外，还提供稳定的
 * display 字段，避免页面把裸 BizId 或原始 JSON 当作主要业务体验。</p>
 */
@Builder
public record AiModelCallAuditView(
    String bizId,
    String callId,
    String operationCode,
    String callStatus,
    String providerCode,
    String modelCode,
    String modelVersion,
    String remoteModel,
    String endpoint,
    String httpMethod,
    Integer httpStatus,
    Long durationMs,
    String systemPromptHash,
    String userPromptHash,
    String responseHash,
    String requestPreview,
    String responsePreview,
    String requestPayload,
    String responsePayload,
    String inputSummary,
    String outputSummary,
    String modelDisplay,
    String businessDisplay,
    String promptDisplay,
    String skillDisplay,
    String durationDisplay,
    String failureDisplay,
    String businessType,
    String businessBizId,
    String businessLabel,
    String taskCode,
    String eventId,
    String runBizId,
    String runNo,
    String reportBizId,
    String promptBizId,
    String promptCode,
    String promptVersion,
    String skillBizId,
    String skillCode,
    String skillVersion,
    String modelSkillBindingBizId,
    String scenarioCode,
    String environment,
    String errorCode,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
