package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModelSkillBinding;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelSkillBindingEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiModelSkillBindingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI 模型 Skill 绑定仓储实现。 */
@Repository
@RequiredArgsConstructor
public class AiModelSkillBindingStoreImpl implements AiModelSkillBindingStore {
    private final AiModelSkillBindingMapper mapper;

    /** 保存模型 Skill 绑定。 */
    @Override
    public AiModelSkillBinding save(AiModelSkillBinding binding) {
        mapper.save(toEntity(binding));
        return binding;
    }

    /** 根据业务 ID 查询绑定。 */
    @Override
    public Optional<AiModelSkillBinding> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 根据模型、Skill 和场景查询绑定。 */
    @Override
    public Optional<AiModelSkillBinding> findByModelSkillAndScenario(
        String modelBizId,
        String skillBizId,
        String scenarioCode
    ) {
        return Optional.ofNullable(mapper.selectByModelSkillAndScenario(modelBizId, skillBizId, scenarioCode))
            .map(this::toDomain);
    }

    /** 查询模型下启用的 Skill 绑定。 */
    @Override
    public List<AiModelSkillBinding> findEnabledByModelBizId(String modelBizId) {
        return mapper.selectEnabledByModelBizId(modelBizId).stream().map(this::toDomain).toList();
    }

    /** 分页查询模型 Skill 绑定。 */
    @Override
    public PageResult<AiModelSkillBinding> search(AiModelSkillBindingSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<AiModelSkillBinding> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<AiModelSkillBinding>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为数据库列名白名单。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "modelCode" -> "b.model_code";
            case "skillCode" -> "b.skill_code";
            case "scenarioCode" -> "b.scenario_code";
            case "priority" -> "b.priority";
            case "enabled" -> "b.enabled";
            default -> "b.updated_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private AiModelSkillBindingEntity toEntity(AiModelSkillBinding binding) {
        return AiModelSkillBindingEntity.builder()
            .bizId(binding.bizId())
            .modelBizId(binding.modelBizId())
            .modelCode(binding.modelCode())
            .modelVersion(binding.modelVersion())
            .skillBizId(binding.skillBizId())
            .skillCode(binding.skillCode())
            .skillVersion(binding.skillVersion())
            .scenarioCode(binding.scenarioCode())
            .priority(binding.priority())
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
    private AiModelSkillBinding toDomain(AiModelSkillBindingEntity entity) {
        return AiModelSkillBinding.builder()
            .bizId(entity.getBizId())
            .modelBizId(entity.getModelBizId())
            .modelCode(entity.getModelCode())
            .modelVersion(entity.getModelVersion())
            .skillBizId(entity.getSkillBizId())
            .skillCode(entity.getSkillCode())
            .skillVersion(entity.getSkillVersion())
            .scenarioCode(entity.getScenarioCode())
            .priority(entity.getPriority())
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
