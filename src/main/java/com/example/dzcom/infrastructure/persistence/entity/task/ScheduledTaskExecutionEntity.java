package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 配置驱动任务执行记录持久化实体。 */
@TableName("aiw_scheduled_task_execution")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledTaskExecutionEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String taskCode;
    private String taskType;
    private String triggerSource;
    private String status;
    private String eventId;
    private String resultSummary;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
