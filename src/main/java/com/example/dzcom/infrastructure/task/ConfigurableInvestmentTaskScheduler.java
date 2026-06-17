package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.service.task.InvestmentTaskManagementService;
import com.example.dzcom.application.service.task.InvestmentTaskScheduleRefreshPort;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/** 根据配置列表动态注册投资任务 Cron。 */
@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("investmentTaskDefinitionInitializer")
@ConditionalOnProperty(prefix = "investment.tasks", name = "enabled", havingValue = "true")
public class ConfigurableInvestmentTaskScheduler implements InvestmentTaskScheduleRefreshPort {
    private final InvestmentTaskDefinitionStore definitions;
    private final InvestmentTaskManagementService tasks;
    private final TaskScheduler investmentTaskScheduler;
    private final Map<String, ScheduledFuture<?>> schedules = new ConcurrentHashMap<>();

    /** 应用启动时按配置生成全部启用任务。 */
    @PostConstruct
    public void registerTasks() {
        refreshSchedules();
    }

    /** 重新加载数据库中的启用任务并刷新 Cron 注册。 */
    @Override
    public synchronized void refreshSchedules() {
        schedules.values().forEach(future -> future.cancel(false));
        schedules.clear();
        definitions.findAll().stream()
            .filter(InvestmentTaskDefinition::enabled)
            .forEach(definition -> {
                validate(definition);
                ScheduledFuture<?> future = investmentTaskScheduler.schedule(
                    () -> tasks.trigger(definition.taskCode(), definition.parameters(), "SCHEDULE"),
                    new CronTrigger(definition.cron(), ZoneId.of(definition.zone()))
                );
                schedules.put(definition.taskCode(), future);
                log.info("已注册投资任务: code={}, type={}, cron={}, zone={}",
                    definition.taskCode(), definition.taskType(),
                    definition.cron(), definition.zone());
            });
    }

    /** 校验任务定义的必填字段。 */
    private void validate(InvestmentTaskDefinition definition) {
        if (definition.taskCode() == null || definition.taskCode().isBlank()
            || definition.taskType() == null || definition.taskType().isBlank()
            || definition.cron() == null || definition.cron().isBlank()) {
            throw new IllegalArgumentException("投资任务 code、type、cron 不能为空");
        }
    }
}
