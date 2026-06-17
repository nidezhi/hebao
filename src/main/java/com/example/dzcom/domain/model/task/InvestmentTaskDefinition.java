package com.example.dzcom.domain.model.task;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/** 可落库配置的投资定时任务定义。 */
@Builder
public record InvestmentTaskDefinition(
    String bizId,
    String taskCode,
    String taskType,
    String cron,
    String zone,
    boolean enabled,
    Map<String, String> parameters,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
