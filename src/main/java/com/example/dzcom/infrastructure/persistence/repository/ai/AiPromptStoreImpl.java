package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiPromptOutputSchema;
import com.example.dzcom.domain.model.ai.AiPromptTemplate;
import com.example.dzcom.domain.model.ai.AiPromptVariable;
import com.example.dzcom.domain.repository.ai.AiPromptSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiPromptStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptOutputSchemaEntity;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptTemplateEntity;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptVariableEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiPromptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI Prompt 版本化仓储实现。 */
@Repository
@RequiredArgsConstructor
public class AiPromptStoreImpl implements AiPromptStore {
    private final AiPromptMapper mapper;

    /** 保存 Prompt 模板主记录。 */
    @Override
    public AiPromptTemplate saveTemplate(AiPromptTemplate template) {
        mapper.saveTemplate(toTemplateEntity(template));
        return template;
    }

    /** 删除当前版本旧变量后写入新的变量定义，保证前端一次保存后的定义一致。 */
    @Override
    public void replaceVariables(String promptBizId, List<AiPromptVariable> variables) {
        mapper.deleteVariables(promptBizId);
        variables.stream()
            .map(this::toVariableEntity)
            .forEach(mapper::insertVariable);
    }

    /** 删除当前版本旧输出 Schema 后写入新的 Schema 定义。 */
    @Override
    public void replaceOutputSchemas(String promptBizId, List<AiPromptOutputSchema> schemas) {
        mapper.deleteOutputSchemas(promptBizId);
        schemas.stream()
            .map(this::toOutputSchemaEntity)
            .forEach(mapper::insertOutputSchema);
    }

    /** 根据业务 ID 查询 Prompt 模板。 */
    @Override
    public Optional<AiPromptTemplate> findTemplateByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectTemplateByBizId(bizId))
            .map(this::toTemplateDomain);
    }

    /** 根据编码和版本查询 Prompt 模板。 */
    @Override
    public Optional<AiPromptTemplate> findTemplateByCodeAndVersion(String promptCode, String promptVersion) {
        return Optional.ofNullable(mapper.selectTemplateByCodeAndVersion(promptCode, promptVersion))
            .map(this::toTemplateDomain);
    }

    /** 查询模板变量定义。 */
    @Override
    public List<AiPromptVariable> findVariables(String promptBizId) {
        return mapper.selectVariables(promptBizId).stream()
            .map(this::toVariableDomain)
            .toList();
    }

    /** 查询模板输出 Schema 定义。 */
    @Override
    public List<AiPromptOutputSchema> findOutputSchemas(String promptBizId) {
        return mapper.selectOutputSchemas(promptBizId).stream()
            .map(this::toOutputSchemaDomain)
            .toList();
    }

    /** 分页查询 Prompt 模板。 */
    @Override
    public PageResult<AiPromptTemplate> search(AiPromptSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<AiPromptTemplate> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toTemplateDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<AiPromptTemplate>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列，避免 XML 中动态排序列被外部输入控制。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "promptCode" -> "p.prompt_code";
            case "promptVersion" -> "p.prompt_version";
            case "scenario" -> "p.scenario";
            case "status" -> "p.status";
            default -> "p.updated_at";
        };
    }

    /** 将 Prompt 模板领域对象转换为持久化实体。 */
    private AiPromptTemplateEntity toTemplateEntity(AiPromptTemplate template) {
        return AiPromptTemplateEntity.builder()
            .bizId(template.bizId())
            .promptCode(template.promptCode())
            .promptVersion(template.promptVersion())
            .scenario(template.scenario())
            .templateName(template.templateName())
            .templateContent(template.templateContent())
            .status(template.status())
            .description(template.description())
            .createdAt(template.createdAt())
            .updatedAt(template.updatedAt())
            .createdBy(template.createdBy())
            .updatedBy(template.updatedBy())
            .build();
    }

    /** 将 Prompt 变量领域对象转换为持久化实体。 */
    private AiPromptVariableEntity toVariableEntity(AiPromptVariable variable) {
        return AiPromptVariableEntity.builder()
            .bizId(variable.bizId())
            .promptBizId(variable.promptBizId())
            .variableName(variable.variableName())
            .sourcePath(variable.sourcePath())
            .required(variable.required())
            .description(variable.description())
            .createdAt(variable.createdAt())
            .build();
    }

    /** 将 Prompt 输出 Schema 领域对象转换为持久化实体。 */
    private AiPromptOutputSchemaEntity toOutputSchemaEntity(AiPromptOutputSchema schema) {
        return AiPromptOutputSchemaEntity.builder()
            .bizId(schema.bizId())
            .promptBizId(schema.promptBizId())
            .schemaVersion(schema.schemaVersion())
            .schemaJson(schema.schemaJson())
            .createdAt(schema.createdAt())
            .build();
    }

    /** 将 Prompt 模板持久化实体转换为领域对象。 */
    private AiPromptTemplate toTemplateDomain(AiPromptTemplateEntity entity) {
        return AiPromptTemplate.builder()
            .bizId(entity.getBizId())
            .promptCode(entity.getPromptCode())
            .promptVersion(entity.getPromptVersion())
            .scenario(entity.getScenario())
            .templateName(entity.getTemplateName())
            .templateContent(entity.getTemplateContent())
            .status(entity.getStatus())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }

    /** 将 Prompt 变量持久化实体转换为领域对象。 */
    private AiPromptVariable toVariableDomain(AiPromptVariableEntity entity) {
        return AiPromptVariable.builder()
            .bizId(entity.getBizId())
            .promptBizId(entity.getPromptBizId())
            .variableName(entity.getVariableName())
            .sourcePath(entity.getSourcePath())
            .required(entity.isRequired())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    /** 将 Prompt 输出 Schema 持久化实体转换为领域对象。 */
    private AiPromptOutputSchema toOutputSchemaDomain(AiPromptOutputSchemaEntity entity) {
        return AiPromptOutputSchema.builder()
            .bizId(entity.getBizId())
            .promptBizId(entity.getPromptBizId())
            .schemaVersion(entity.getSchemaVersion())
            .schemaJson(entity.getSchemaJson())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
