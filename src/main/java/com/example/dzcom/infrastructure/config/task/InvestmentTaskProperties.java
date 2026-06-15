package com.example.dzcom.infrastructure.config.task;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置驱动投资任务参数。
 *
 * <p>任务列表由 YAML 或环境配置生成，代码只提供稳定任务类型和处理器。</p>
 */
@Data
@ConfigurationProperties(prefix = "investment.tasks")
public class InvestmentTaskProperties {
    /** 是否启用投资任务调度和 Kafka 消费。 */
    private boolean enabled = false;
    /** Kafka 任务触发主题。 */
    private String topic = "dzcom.investment.task.trigger.v1";
    /** 动态任务定义列表。 */
    private List<TaskDefinition> definitions = new ArrayList<>();

    /** 单个可配置任务定义。 */
    @Data
    public static class TaskDefinition {
        /** 稳定任务编码。 */
        private String code;
        /** 任务处理器类型。 */
        private String type;
        /** Spring Cron 表达式。 */
        private String cron;
        /** Cron 时区。 */
        private String zone = "Asia/Shanghai";
        /** 是否启用当前任务。 */
        private boolean enabled = true;
        /** 任务处理器参数。 */
        private Map<String, String> parameters = new LinkedHashMap<>();
    }
}
