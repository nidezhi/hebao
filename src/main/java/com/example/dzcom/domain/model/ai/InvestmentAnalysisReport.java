package com.example.dzcom.domain.model.ai;

import lombok.Builder;

import java.time.LocalDateTime;

/** 可追溯的投资分析报告。 */
@Builder
public record InvestmentAnalysisReport(
    String bizId,
    String requestId,
    String providerCode,
    String modelCode,
    String marketScope,
    String themeCode,
    String themeName,
    String status,
    String investmentSummary,
    String trend,
    String investmentPlan,
    String simulatedReturn,
    String chartPayload,
    String promptSnapshot,
    String failureReason,
    LocalDateTime generatedAt,
    LocalDateTime createdAt
) {
}
