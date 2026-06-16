package com.example.dzcom.domain.repository.task;

import java.time.LocalDateTime;

/** 定时任务执行记录分页筛选条件。 */
public record ScheduledTaskExecutionSearchCriteria(
    String taskCode,
    String taskType,
    String status,
    LocalDateTime startedFrom,
    LocalDateTime startedTo,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
