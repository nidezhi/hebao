package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 记录 Kafka 任务事件触发、执行状态、结果摘要和失败原因的持久化实体。 */
@Schema(description = "配置驱动任务执行记录持久化实体")
@TableName("aiw_scheduled_task_execution")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledTaskExecutionEntity {
    /** 执行记录业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "执行记录业务唯一标识")
    private String bizId;
    /** 被执行的任务稳定编码。 */
    @Schema(description = "任务编码")
    private String taskCode;
    /** 用于匹配任务处理器的任务类型。 */
    @Schema(description = "任务类型")
    private String taskType;
    /** 任务触发来源。 */
    @Schema(description = "触发来源：SCHEDULE/MANUAL/RETRY")
    private String triggerSource;
    /** 当前执行状态。 */
    @Schema(description = "执行状态：RUNNING/SUCCEEDED/FAILED")
    private String status;
    /** Kafka 事件唯一标识和消费幂等键。 */
    @Schema(description = "Kafka 事件唯一标识")
    private String eventId;
    /** 任务成功后的可读结果摘要。 */
    @Schema(description = "执行结果摘要")
    private String resultSummary;
    /** 任务失败后的原因摘要；完整堆栈记录在日志中。 */
    @Schema(description = "失败原因摘要")
    private String failureReason;
    /** 任务开始执行时间，北京时间。 */
    @Schema(description = "任务开始时间，北京时间")
    private LocalDateTime startedAt;
    /** 任务完成或失败时间，北京时间。 */
    @Schema(description = "任务完成时间，北京时间")
    private LocalDateTime completedAt;
    /** 执行记录创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
}
