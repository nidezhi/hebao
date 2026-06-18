package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 用于幂等控制、运行状态追踪和失败审计的任务执行记录。 */
@Builder(toBuilder = true)
@Schema(description = "配置驱动任务执行记录领域对象")
public record ScheduledTaskExecution(
    @Schema(description = "执行记录业务唯一标识")
    String bizId,
    @Schema(description = "任务编码")
    String taskCode,
    @Schema(description = "任务类型")
    String taskType,
    @Schema(description = "触发来源")
    String triggerSource,
    @Schema(description = "执行状态")
    String status,
    @Schema(description = "Kafka 事件唯一标识和消费幂等键")
    String eventId,
    @Schema(description = "执行结果摘要")
    String resultSummary,
    @Schema(description = "失败原因摘要")
    String failureReason,
    @Schema(description = "任务开始时间，北京时间")
    LocalDateTime startedAt,
    @Schema(description = "任务完成时间，北京时间")
    LocalDateTime completedAt,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt
) {
}
