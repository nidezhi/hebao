package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentTaskDefinitionEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.InvestmentTaskDefinitionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 投资定时任务定义仓储实现。 */
@Repository
@RequiredArgsConstructor
public class InvestmentTaskDefinitionStoreImpl implements InvestmentTaskDefinitionStore {
    private static final TypeReference<Map<String, String>> PARAMETER_TYPE = new TypeReference<>() {
    };

    private final InvestmentTaskDefinitionMapper mapper;
    private final ObjectMapper objectMapper;

    /** 查询全部任务定义。 */
    @Override
    public List<InvestmentTaskDefinition> findAll() {
        return mapper.selectAll().stream()
            .map(this::toDomain)
            .toList();
    }

    /** 根据稳定任务编码查询任务定义。 */
    @Override
    public Optional<InvestmentTaskDefinition> findByCode(String taskCode) {
        return Optional.ofNullable(mapper.selectByCode(taskCode)).map(this::toDomain);
    }

    /** 新增或更新任务定义。 */
    @Override
    public InvestmentTaskDefinition save(InvestmentTaskDefinition definition) {
        mapper.save(InvestmentTaskDefinitionEntity.builder()
            .bizId(definition.bizId())
            .taskCode(definition.taskCode())
            .taskType(definition.taskType())
            .cron(definition.cron())
            .zone(definition.zone())
            .enabled(definition.enabled())
            .parameters(writeParameters(definition.parameters()))
            .description(definition.description())
            .createdAt(definition.createdAt())
            .updatedAt(definition.updatedAt())
            .build());
        return definition;
    }

    /** 将持久化实体转换为领域对象。 */
    private InvestmentTaskDefinition toDomain(InvestmentTaskDefinitionEntity entity) {
        return InvestmentTaskDefinition.builder()
            .bizId(entity.getBizId())
            .taskCode(entity.getTaskCode())
            .taskType(entity.getTaskType())
            .cron(entity.getCron())
            .zone(entity.getZone())
            .enabled(entity.isEnabled())
            .parameters(readParameters(entity.getParameters()))
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /** 解析任务参数 JSON。 */
    private Map<String, String> readParameters(String parameters) {
        try {
            return parameters == null || parameters.isBlank()
                ? new LinkedHashMap<>()
                : objectMapper.readValue(parameters, PARAMETER_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("任务参数反序列化失败", exception);
        }
    }

    /** 序列化任务参数 JSON。 */
    private String writeParameters(Map<String, String> parameters) {
        try {
            return objectMapper.writeValueAsString(
                parameters == null ? new LinkedHashMap<>() : parameters);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("任务参数序列化失败", exception);
        }
    }
}
