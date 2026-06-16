package com.example.dzcom.application.dto.task;

import lombok.Builder;

import java.time.LocalDateTime;

/** 投资任务触发结果。 */
@Builder
public record InvestmentTaskTriggerResult(
    String eventId,
    String taskCode,
    String taskType,
    String triggerSource,
    LocalDateTime triggeredAt
) {
}
