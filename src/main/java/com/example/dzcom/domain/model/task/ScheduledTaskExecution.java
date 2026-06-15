package com.example.dzcom.domain.model.task;

import lombok.Builder;

import java.time.LocalDateTime;

/** 配置驱动任务执行记录。 */
@Builder(toBuilder = true)
public record ScheduledTaskExecution(
    String bizId,
    String taskCode,
    String taskType,
    String triggerSource,
    String status,
    String eventId,
    String resultSummary,
    String failureReason,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {
}
