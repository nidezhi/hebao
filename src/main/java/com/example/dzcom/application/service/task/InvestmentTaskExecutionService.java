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
    private final List<InvestmentTaskHandler> handlers;
    private final ScheduledTaskExecutionStore executions;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 执行单个 Kafka 投资任务事件。 */
    public void execute(InvestmentTaskEvent event) {
        if (executions.findByEventId(event.eventId())
            .filter(existing -> "SUCCEEDED".equals(existing.status()))
            .isPresent()) {
            log.info("跳过已成功执行的投资任务事件: eventId={}", event.eventId());
            return;
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
            executions.save(running.toBuilder()
                .status("SUCCEEDED")
                .resultSummary(limit(summary, 1024))
                .completedAt(clock.now())
                .build());
        } catch (Exception exception) {
            executions.save(running.toBuilder()
                .status("FAILED")
                .failureReason(limit(exception.getMessage(), 2048))
                .completedAt(clock.now())
                .build());
            log.error("投资任务执行失败: eventId={}, taskCode={}, taskType={}",
                event.eventId(), event.taskCode(), event.taskType(), exception);
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
