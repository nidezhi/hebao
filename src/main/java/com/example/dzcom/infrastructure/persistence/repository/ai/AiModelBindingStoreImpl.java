package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModelBinding;
import com.example.dzcom.domain.repository.ai.AiModelBindingSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelBindingStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelBindingEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiModelBindingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI 模型挂靠配置仓储实现。 */
@Repository
@RequiredArgsConstructor
public class AiModelBindingStoreImpl implements AiModelBindingStore {
    private final AiModelBindingMapper mapper;

    /** 保存模型挂靠配置。 */
    @Override
    public AiModelBinding save(AiModelBinding binding) {
        mapper.save(toEntity(binding));
        return binding;
    }

    /** 根据业务 ID 查询模型挂靠配置。 */
    @Override
    public Optional<AiModelBinding> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 根据场景和环境查询模型挂靠配置。 */
    @Override
    public Optional<AiModelBinding> findByScenarioAndEnvironment(String scenarioCode, String environment) {
        return Optional.ofNullable(mapper.selectByScenarioAndEnvironment(scenarioCode, environment))
            .map(this::toDomain);
    }

    /** 分页查询模型挂靠配置。 */
    @Override
    public PageResult<AiModelBinding> search(AiModelBindingSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<AiModelBinding> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<AiModelBinding>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 转换排序字段为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "scenarioCode" -> "b.scenario_code";
            case "modelCode" -> "b.model_code";
            case "providerCode" -> "b.provider_code";
            case "environment" -> "b.environment";
            case "enabled" -> "b.enabled";
            default -> "b.updated_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private AiModelBindingEntity toEntity(AiModelBinding binding) {
        return AiModelBindingEntity.builder()
            .bizId(binding.bizId())
            .scenarioCode(binding.scenarioCode())
            .scenarioName(binding.scenarioName())
            .modelCode(binding.modelCode())
            .providerCode(binding.providerCode())
            .environment(binding.environment())
            .enabled(binding.enabled())
            .config(binding.config())
            .description(binding.description())
            .createdAt(binding.createdAt())
            .updatedAt(binding.updatedAt())
            .createdBy(binding.createdBy())
            .updatedBy(binding.updatedBy())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private AiModelBinding toDomain(AiModelBindingEntity entity) {
        return AiModelBinding.builder()
            .bizId(entity.getBizId())
            .scenarioCode(entity.getScenarioCode())
            .scenarioName(entity.getScenarioName())
            .modelCode(entity.getModelCode())
            .providerCode(entity.getProviderCode())
            .environment(entity.getEnvironment())
            .enabled(entity.isEnabled())
            .config(entity.getConfig())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
