package com.example.dzcom.application.task;

import com.example.dzcom.infrastructure.config.task.InvestmentTaskProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/** 根据配置列表动态注册投资任务 Cron。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "investment.tasks", name = "enabled", havingValue = "true")
public class ConfigurableInvestmentTaskScheduler {
    private final InvestmentTaskProperties properties;
    private final InvestmentTaskEventPublisher publisher;
    private final TaskScheduler investmentTaskScheduler;

    /** 应用启动时按配置生成全部启用任务。 */
    @PostConstruct
    public void registerTasks() {
        properties.getDefinitions().stream()
            .filter(InvestmentTaskProperties.TaskDefinition::isEnabled)
            .forEach(definition -> {
                validate(definition);
                investmentTaskScheduler.schedule(
                    () -> publisher.publish(definition),
                    new CronTrigger(definition.getCron(), ZoneId.of(definition.getZone()))
                );
                log.info("已注册投资任务: code={}, type={}, cron={}, zone={}",
                    definition.getCode(), definition.getType(),
                    definition.getCron(), definition.getZone());
            });
    }

    /** 校验任务定义的必填字段。 */
    private void validate(InvestmentTaskProperties.TaskDefinition definition) {
        if (definition.getCode() == null || definition.getCode().isBlank()
            || definition.getType() == null || definition.getType().isBlank()
            || definition.getCron() == null || definition.getCron().isBlank()) {
            throw new IllegalArgumentException("投资任务 code、type、cron 不能为空");
        }
    }
}
