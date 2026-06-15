package com.example.dzcom.application.task;

import com.example.dzcom.application.service.task.InvestmentTaskEvent;
import com.example.dzcom.application.service.task.InvestmentTaskExecutionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka 投资任务事件消费者。 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "investment.tasks", name = "enabled", havingValue = "true")
public class InvestmentTaskEventConsumer {
    private final InvestmentTaskExecutionService executionService;
    private final ObjectMapper objectMapper;

    /** 消费配置驱动任务触发事件并交给应用服务执行。 */
    @KafkaListener(topics = "${investment.tasks.topic:dzcom.investment.task.trigger.v1}")
    public void consume(String payload) {
        executionService.execute(deserialize(payload));
    }

    /** 将 Kafka JSON 载荷反序列化为任务事件。 */
    private InvestmentTaskEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, InvestmentTaskEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("投资任务事件反序列化失败", exception);
        }
    }
}
