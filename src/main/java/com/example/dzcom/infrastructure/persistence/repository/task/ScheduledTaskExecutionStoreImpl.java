package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import com.example.dzcom.infrastructure.persistence.entity.task.ScheduledTaskExecutionEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.ScheduledTaskExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** 定时任务执行记录仓储实现。 */
@Repository
@RequiredArgsConstructor
public class ScheduledTaskExecutionStoreImpl implements ScheduledTaskExecutionStore {
    private final ScheduledTaskExecutionMapper mapper;

    /** 根据事件 ID 查询任务执行记录。 */
    @Override
    public Optional<ScheduledTaskExecution> findByEventId(String eventId) {
        return Optional.ofNullable(mapper.selectByEventId(eventId)).map(this::toDomain);
    }

    /** 保存或更新任务执行记录。 */
    @Override
    public ScheduledTaskExecution save(ScheduledTaskExecution execution) {
        mapper.save(ScheduledTaskExecutionEntity.builder()
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
            .build());
        return execution;
    }

    /** 将持久化实体转换为领域对象。 */
    private ScheduledTaskExecution toDomain(ScheduledTaskExecutionEntity entity) {
        return ScheduledTaskExecution.builder()
            .bizId(entity.getBizId())
            .taskCode(entity.getTaskCode())
            .taskType(entity.getTaskType())
            .triggerSource(entity.getTriggerSource())
            .status(entity.getStatus())
            .eventId(entity.getEventId())
            .resultSummary(entity.getResultSummary())
            .failureReason(entity.getFailureReason())
            .startedAt(entity.getStartedAt())
            .completedAt(entity.getCompletedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
