package com.example.dzcom.application.service.task;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/** Kafka 投资任务触发事件。 */
@Builder
public record InvestmentTaskEvent(
    String eventId,
    String taskCode,
    String taskType,
    String triggerSource,
    Map<String, String> parameters,
    LocalDateTime triggeredAt
) {
}
