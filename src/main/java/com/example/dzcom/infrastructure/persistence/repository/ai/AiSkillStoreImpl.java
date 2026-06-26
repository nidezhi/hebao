package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiSkill;
import com.example.dzcom.domain.repository.ai.AiSkillSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiSkillStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiSkillEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiSkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI Skill 仓储实现。 */
@Repository
@RequiredArgsConstructor
public class AiSkillStoreImpl implements AiSkillStore {
    private final AiSkillMapper mapper;

    /** 保存 Skill 版本。 */
    @Override
    public AiSkill save(AiSkill skill) {
        mapper.save(toEntity(skill));
        return skill;
    }

    /** 根据业务 ID 查询 Skill。 */
    @Override
    public Optional<AiSkill> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 根据编码和版本查询 Skill。 */
    @Override
    public Optional<AiSkill> findByCodeAndVersion(String skillCode, String skillVersion) {
        return Optional.ofNullable(mapper.selectByCodeAndVersion(skillCode, skillVersion)).map(this::toDomain);
    }

    /** 查询指定编码最近启用的 Skill 版本。 */
    @Override
    public Optional<AiSkill> findActiveByCode(String skillCode) {
        return Optional.ofNullable(mapper.selectActiveByCode(skillCode)).map(this::toDomain);
    }

    /** 分页查询 Skill。 */
    @Override
    public PageResult<AiSkill> search(AiSkillSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<AiSkill> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<AiSkill>builder()
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
            case "skillCode" -> "s.skill_code";
            case "skillVersion" -> "s.skill_version";
            case "skillType" -> "s.skill_type";
            case "status" -> "s.status";
            default -> "s.updated_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private AiSkillEntity toEntity(AiSkill skill) {
        return AiSkillEntity.builder()
            .bizId(skill.bizId())
            .skillCode(skill.skillCode())
            .skillVersion(skill.skillVersion())
            .skillName(skill.skillName())
            .skillType(skill.skillType())
            .status(skill.status())
            .instructionContent(skill.instructionContent())
            .inputSchema(skill.inputSchema())
            .outputSchema(skill.outputSchema())
            .evaluationPolicy(skill.evaluationPolicy())
            .description(skill.description())
            .createdAt(skill.createdAt())
            .updatedAt(skill.updatedAt())
            .createdBy(skill.createdBy())
            .updatedBy(skill.updatedBy())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private AiSkill toDomain(AiSkillEntity entity) {
        return AiSkill.builder()
            .bizId(entity.getBizId())
            .skillCode(entity.getSkillCode())
            .skillVersion(entity.getSkillVersion())
            .skillName(entity.getSkillName())
            .skillType(entity.getSkillType())
            .status(entity.getStatus())
            .instructionContent(entity.getInstructionContent())
            .inputSchema(entity.getInputSchema())
            .outputSchema(entity.getOutputSchema())
            .evaluationPolicy(entity.getEvaluationPolicy())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
