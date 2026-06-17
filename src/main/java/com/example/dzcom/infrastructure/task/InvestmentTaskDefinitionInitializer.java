package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import com.example.dzcom.infrastructure.config.task.InvestmentTaskProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/** 将 YAML 或环境变量提供的任务定义同步为数据库默认配置。 */
@Component
@RequiredArgsConstructor
public class InvestmentTaskDefinitionInitializer {
    private final InvestmentTaskProperties properties;
    private final InvestmentTaskDefinitionStore definitions;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 应用启动时将外部配置解析结果写入数据库。 */
    @PostConstruct
    public void initializeDefinitions() {
        LocalDateTime now = clock.now();
        properties.getDefinitions().forEach(source -> {
            InvestmentTaskDefinition existed = definitions.findByCode(source.getCode()).orElse(null);
            if (existed != null) {
                return;
            }
            definitions.save(InvestmentTaskDefinition.builder()
                .bizId(ids.newBizId())
                .taskCode(source.getCode())
                .taskType(source.getType())
                .cron(source.getCron())
                .zone(source.getZone())
                .enabled(source.isEnabled())
                .parameters(new LinkedHashMap<>(source.getParameters()))
                .description("由应用配置初始化")
                .createdAt(now)
                .updatedAt(now)
                .build());
        });
    }
}
