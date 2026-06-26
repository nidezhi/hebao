package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 定时任务执行记录响应。 */
@Builder
@Schema(description = "定时任务执行记录响应")
public record ScheduledTaskExecutionResponse(
    @Schema(description = "执行记录业务 ID") String bizId,
    @Schema(description = "任务编码", example = "llm-product-nav-collection") String taskCode,
    @Schema(description = "任务类型", example = "AI_DATA_SOURCE_DISCOVERY") String taskType,
    @Schema(description = "触发来源：SCHEDULE/MANUAL", example = "SCHEDULE") String triggerSource,
    @Schema(description = "执行状态：RUNNING/SUCCEEDED/BLOCKED/FAILED", example = "SUCCEEDED") String status,
    @Schema(description = "Kafka 事件 ID") String eventId,
    @Schema(description = "执行结果摘要") String resultSummary,
    @Schema(description = "失败原因；成功时为空") String failureReason,
    @Schema(description = "开始时间") LocalDateTime startedAt,
    @Schema(description = "完成时间") LocalDateTime completedAt,
    @Schema(description = "创建时间") LocalDateTime createdAt
) {
    /** 将领域对象转换为接口响应。 */
    public static ScheduledTaskExecutionResponse from(ScheduledTaskExecution execution) {
        return ScheduledTaskExecutionResponse.builder()
            .bizId(execution.bizId())
            .taskCode(execution.taskCode())
            .taskType(execution.taskType())
            .triggerSource(execution.triggerSource())
            .status(execution.status())
            .eventId(execution.eventId())
            .resultSummary(execution.resultSummary())
            .failureReason(execution.failureReason())
            .startedAt(execution.startedAt())
            .completedAt(execution.completedAt())
            .createdAt(execution.createdAt())
            .build();
    }
}
