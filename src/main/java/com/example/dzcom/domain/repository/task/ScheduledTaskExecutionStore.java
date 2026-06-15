package com.example.dzcom.domain.repository.task;

import com.example.dzcom.domain.model.task.ScheduledTaskExecution;

import java.util.Optional;

/** 定时任务执行记录仓储端口。 */
public interface ScheduledTaskExecutionStore {
    /** 根据 Kafka 事件 ID 查询执行记录，用于消费幂等。 */
    Optional<ScheduledTaskExecution> findByEventId(String eventId);

    /** 保存或更新任务执行记录。 */
    ScheduledTaskExecution save(ScheduledTaskExecution execution);
}
