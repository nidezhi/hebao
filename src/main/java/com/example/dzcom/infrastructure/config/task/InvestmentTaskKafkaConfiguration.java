package com.example.dzcom.infrastructure.config.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

/** 配置驱动投资任务和 Kafka 基础配置。 */
@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "investment.tasks", name = "enabled", havingValue = "true")
public class InvestmentTaskKafkaConfiguration {

    /** 创建字符串 Kafka ProducerFactory。 */
    @Bean
    public ProducerFactory<String, String> investmentTaskProducerFactory(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    /** 创建投资任务 KafkaTemplate。 */
    @Bean
    public KafkaTemplate<String, String> investmentTaskKafkaTemplate(
        ProducerFactory<String, String> investmentTaskProducerFactory
    ) {
        return new KafkaTemplate<>(investmentTaskProducerFactory);
    }

    /** 创建字符串 Kafka ConsumerFactory。 */
    @Bean
    public ConsumerFactory<String, String> investmentTaskConsumerFactory(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
        @Value("${spring.kafka.consumer.group-id}") String groupId
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    /** 创建 Kafka Listener 容器工厂并使用逐条确认模式。 */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> investmentTaskConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(investmentTaskConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    /** 创建投资任务专用调度线程池。 */
    @Bean
    public TaskScheduler investmentTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("investment-task-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        return scheduler;
    }
}
