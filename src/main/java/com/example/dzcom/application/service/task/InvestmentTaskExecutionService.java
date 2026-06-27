package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** 投资任务执行编排服务，负责幂等、处理器匹配和执行审计。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentTaskExecutionService {
    private static final int FAILURE_REASON_MAX_LENGTH = 12_000;

    private final List<InvestmentTaskHandler> handlers;
    private final ScheduledTaskExecutionStore executions;
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
        ScheduledTaskExecution running = ScheduledTaskExecution.builder()
            .bizId(ids.newBizId())
            .taskCode(event.taskCode())
            .taskType(event.taskType())
            .triggerSource(event.triggerSource())
            .status("RUNNING")
            .eventId(event.eventId())
            .startedAt(now)
            .createdAt(now)
            .build();
        executions.save(running);
        try {
            InvestmentTaskHandler handler = handlers.stream()
                .filter(candidate -> candidate.supports(event.taskType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "未找到投资任务处理器: " + event.taskType()));
            String summary = handler.execute(event);
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
                event.eventId(), event.taskCode(), event.taskType(), exception.getMessage());
            return blocked;
        } catch (Exception exception) {
            ScheduledTaskExecution failed = executions.save(running.toBuilder()
                .status("FAILED")
                .failureReason(limit(exception.getMessage(), FAILURE_REASON_MAX_LENGTH))
                .completedAt(clock.now())
                .build());
            log.error("投资任务执行失败: eventId={}, taskCode={}, taskType={}",
                event.eventId(), event.taskCode(), event.taskType(), exception);
            return failed;
        }
    }

    /** 限制审计文本长度，避免异常消息撑大记录。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
