package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.task.ScheduledTaskExecutionEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.ScheduledTaskExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /** 根据筛选条件分页查询任务执行记录。 */
    @Override
    public PageResult<ScheduledTaskExecution> search(ScheduledTaskExecutionSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<ScheduledTaskExecution> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<ScheduledTaskExecution>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "taskCode" -> "e.task_code";
            case "taskType" -> "e.task_type";
            case "status" -> "e.status";
            case "completedAt" -> "e.completed_at";
            case "createdAt" -> "e.created_at";
            default -> "e.started_at";
        };
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
