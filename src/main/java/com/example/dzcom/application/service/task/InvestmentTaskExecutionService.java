package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 投资任务执行编排服务，负责幂等、处理器匹配和执行审计。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentTaskExecutionService {
    private static final int FAILURE_REASON_MAX_LENGTH = 12_000;

    private final List<InvestmentTaskHandler> handlers;
    private final ScheduledTaskExecutionStore executions;
    private final Optional<InvestmentTaskDefinitionStore> definitions;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 执行单个 Kafka 投资任务事件。 */
    public void execute(InvestmentTaskEvent event) {
        executeAndReturn(event);
    }

    /**
     * 同步执行单个投资任务事件，并返回本次执行结果。
     *
     * <p>Kafka 消费入口仍使用 {@link #execute(InvestmentTaskEvent)}。自动闭环编排需要
     * 明确知道每个子任务是否成功，因此使用该方法把状态、摘要和失败原因带回运行审计。</p>
     *
     * @param event 投资任务事件
     * @return 执行结果领域对象
     * @author dz
     * @date 2026-06-25
     */
    public ScheduledTaskExecution executeAndReturn(InvestmentTaskEvent event) {
        if (executions.findByEventId(event.eventId())
            .filter(existing -> "SUCCEEDED".equals(existing.status()))
            .isPresent()) {
            log.info("跳过已成功执行的投资任务事件: eventId={}", event.eventId());
            return executions.findByEventId(event.eventId()).orElseThrow();
        }
        LocalDateTime now = clock.now();
        InvestmentTaskEvent effectiveEvent = effectiveEvent(event);
        ScheduledTaskExecution running = ScheduledTaskExecution.builder()
            .bizId(ids.newBizId())
            .taskCode(effectiveEvent.taskCode())
            .taskType(effectiveEvent.taskType())
            .triggerSource(effectiveEvent.triggerSource())
            .status("RUNNING")
            .eventId(effectiveEvent.eventId())
            .startedAt(now)
            .createdAt(now)
            .build();
        executions.save(running);
        try {
            if (shouldSkipDisabledScheduledTask(effectiveEvent)) {
                String summary = "定时任务已禁用，跳过旧调度或历史Kafka消息";
                ScheduledTaskExecution skipped = executions.save(running.toBuilder()
                    .status("SKIPPED")
                    .resultSummary(summary)
                    .completedAt(clock.now())
                    .build());
                log.warn("跳过已禁用的定时投资任务事件: eventId={}, taskCode={}, taskType={}, triggerSource={}",
                    effectiveEvent.eventId(), effectiveEvent.taskCode(), effectiveEvent.taskType(), effectiveEvent.triggerSource());
                return skipped;
            }
            InvestmentTaskHandler handler = handlers.stream()
                .filter(candidate -> candidate.supports(effectiveEvent.taskType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "未找到投资任务处理器: " + effectiveEvent.taskType()));
            String summary = handler.execute(effectiveEvent);
            ScheduledTaskExecution succeeded = executions.save(running.toBuilder()
                .status("SUCCEEDED")
                .resultSummary(limit(summary, 1024))
                .completedAt(clock.now())
                .build());
            return succeeded;
        } catch (InvestmentTaskBlockedException exception) {
            ScheduledTaskExecution blocked = executions.save(running.toBuilder()
                .status("BLOCKED")
                .failureReason(limit(exception.getMessage(), FAILURE_REASON_MAX_LENGTH))
                .completedAt(clock.now())
                .build());
            log.warn("投资任务被业务门禁阻断: eventId={}, taskCode={}, taskType={}, reason={}",
                effectiveEvent.eventId(), effectiveEvent.taskCode(), effectiveEvent.taskType(), exception.getMessage());
            return blocked;
        } catch (Exception exception) {
            ScheduledTaskExecution failed = executions.save(running.toBuilder()
                .status("FAILED")
                .failureReason(limit(exception.getMessage(), FAILURE_REASON_MAX_LENGTH))
                .completedAt(clock.now())
                .build());
            log.error("投资任务执行失败: eventId={}, taskCode={}, taskType={}",
                effectiveEvent.eventId(), effectiveEvent.taskCode(), effectiveEvent.taskType(), exception);
            return failed;
        }
    }

    /** 定时/Kafka事件执行前重新读取当前任务定义，避免历史消息携带旧高消费参数继续运行。 */
    private InvestmentTaskEvent effectiveEvent(InvestmentTaskEvent event) {
        if (definitions.isEmpty()) {
            return event;
        }
        return definitions.get().findByCode(event.taskCode())
            .map(definition -> shouldUseCurrentDefinition(event, definition) ? rebuildFromDefinition(event, definition) : event)
            .orElse(event);
    }

    /** 已禁用的定时任务不消费历史消息；手动触发仍允许显式执行。 */
    private boolean shouldSkipDisabledScheduledTask(InvestmentTaskEvent event) {
        if (definitions.isEmpty()) {
            return false;
        }
        return definitions.get().findByCode(event.taskCode())
            .map(definition -> !definition.enabled()
                && (!isManualTrigger(event) || isStaleEvent(event, definition)))
            .orElse(false);
    }

    /** 旧 Kafka 事件必须以当前任务定义为准，防止历史参数继续触发高消费逻辑。 */
    private boolean shouldUseCurrentDefinition(InvestmentTaskEvent event, InvestmentTaskDefinition definition) {
        return !isManualTrigger(event) || isStaleEvent(event, definition);
    }

    /** 判断事件是否早于当前任务定义。 */
    private boolean isStaleEvent(InvestmentTaskEvent event, InvestmentTaskDefinition definition) {
        return event.triggeredAt() != null
            && definition.updatedAt() != null
            && event.triggeredAt().isBefore(definition.updatedAt());
    }

    /** 按当前任务定义重建事件，只保留事件ID、触发来源和触发时间用于审计。 */
    private InvestmentTaskEvent rebuildFromDefinition(InvestmentTaskEvent event, InvestmentTaskDefinition definition) {
        return InvestmentTaskEvent.builder()
            .eventId(event.eventId())
            .taskCode(event.taskCode())
            .taskType(definition.taskType())
            .triggerSource(event.triggerSource())
            .parameters(new LinkedHashMap<>(definition.parameters() == null ? Map.of() : definition.parameters()))
            .triggeredAt(event.triggeredAt())
            .build();
    }

    /** 判断是否人工触发。 */
    private boolean isManualTrigger(InvestmentTaskEvent event) {
        return "MANUAL".equalsIgnoreCase(event.triggerSource());
    }

    /** 限制审计文本长度，避免异常消息撑大记录。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
