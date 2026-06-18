package com.example.dzcom.application.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资任务触发结果。 */
@Builder
@Schema(description = "投资任务触发应用层结果")
public record InvestmentTaskTriggerResult(
    @Schema(description = "Kafka 事件唯一标识和消费幂等键")
    String eventId,
    @Schema(description = "任务稳定编码")
    String taskCode,
    @Schema(description = "任务处理器类型")
    String taskType,
    @Schema(description = "任务触发来源")
    String triggerSource,
    @Schema(description = "任务触发时间，北京时间")
    LocalDateTime triggeredAt
) {
}
