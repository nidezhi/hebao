package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.service.task.InvestmentTaskEvent;
import com.example.dzcom.application.service.task.InvestmentTaskTriggerPort;
import com.example.dzcom.infrastructure.config.task.InvestmentTaskProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** 投资任务 Kafka 事件发布器。 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "investment.tasks", name = "enabled", havingValue = "true")
public class InvestmentTaskEventPublisher implements InvestmentTaskTriggerPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final InvestmentTaskProperties properties;
    private final ObjectMapper objectMapper;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 根据配置定义发布一次调度触发事件。 */
    public String publish(InvestmentTaskProperties.TaskDefinition definition) {
        InvestmentTaskEvent event = InvestmentTaskEvent.builder()
            .eventId(ids.newBizId())
            .taskCode(definition.getCode())
            .taskType(definition.getType())
            .triggerSource("SCHEDULE")
            .parameters(definition.getParameters())
            .triggeredAt(clock.now())
            .build();
        return publish(event);
    }

    /** 发布一次投资任务触发事件。 */
    @Override
    public String publish(InvestmentTaskEvent event) {
        kafkaTemplate.send(properties.getTopic(), event.taskCode(), serialize(event));
        return event.eventId();
    }

    /** 将任务事件序列化为 JSON。 */
    private String serialize(InvestmentTaskEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("投资任务事件序列化失败", exception);
        }
    }
}
