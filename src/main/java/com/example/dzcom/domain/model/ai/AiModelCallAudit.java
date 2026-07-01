package com.example.dzcom.domain.model.ai;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * AI 模型单次调用审计记录。
 *
 * <p>一条记录覆盖模型调用从开始到结束的生命周期，用于追踪模型、Prompt、Skill、
 * 业务对象、输入输出摘要和失败上下文。</p>
 */
@Builder(toBuilder = true)
public record AiModelCallAudit(
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
