package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.command.ai.AiPromptOutputSchemaCommand;
import com.example.dzcom.application.command.ai.AiPromptPreviewCommand;
import com.example.dzcom.application.command.ai.AiPromptVariableCommand;
import com.example.dzcom.application.command.ai.SaveAiPromptTemplateCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiPromptOutputSchemaView;
import com.example.dzcom.application.dto.ai.AiPromptPreviewView;
import com.example.dzcom.application.dto.ai.AiPromptTemplateView;
import com.example.dzcom.application.dto.ai.AiPromptVariableView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.model.ai.AiPromptOutputSchema;
import com.example.dzcom.domain.model.ai.AiPromptTemplate;
import com.example.dzcom.domain.model.ai.AiPromptVariable;
import com.example.dzcom.domain.repository.ai.AiPromptSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiPromptStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** AI Prompt 模板版本、变量、输出约束和本地预览应用服务。 */
@Service
@RequiredArgsConstructor
public class AiPromptApplicationService {
    private static final String DEFAULT_STATUS = "DRAFT";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String RETIRED_STATUS = "RETIRED";
    private static final Set<String> STATUSES = Set.of("DRAFT", "VALIDATING", ACTIVE_STATUS, RETIRED_STATUS);
    private static final Set<String> SCENARIOS = Set.of(
        "INVESTMENT_REPORT", "INVESTMENT_PLAN", "RISK_AUDIT", "BACKTEST_FEEDBACK");
    private static final Set<String> SORTS =
        Set.of("updatedAt", "promptCode", "promptVersion", "scenario", "status");
    private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{0,63}$");

    private final AiPromptStore prompts;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 新增或更新一个 Prompt 模板版本，并整体替换变量和输出 Schema 定义。
     *
     * @param command Prompt 模板、变量和输出约束命令
     * @return 保存后的 Prompt 模板完整视图
     * @throws BusinessException 当编码、版本、场景、状态、变量或 Schema 不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public AiPromptTemplateView save(SaveAiPromptTemplateCommand command) {
        CurrentOperator operator = currentOperator.required();
        String promptCode = normalizeCode(command.promptCode(), "Prompt编码不能为空");
        String promptVersion = normalizeText(command.promptVersion(), "Prompt版本不能为空");
        String scenario = normalizeAllowed(command.scenario(), SCENARIOS, "Prompt使用场景不合法");
        String status = normalizeAllowed(defaultIfBlank(command.status(), DEFAULT_STATUS), STATUSES, "Prompt状态不合法");
        String templateContent = normalizeText(command.templateContent(), "Prompt模板内容不能为空");
        List<AiPromptVariableCommand> variableCommands = command.variables() == null ? List.of() : command.variables();
        List<AiPromptOutputSchemaCommand> schemaCommands =
            command.outputSchemas() == null ? List.of() : command.outputSchemas();
        validateVariableDefinitions(variableCommands);
        validateOutputSchemas(schemaCommands);

        AiPromptTemplate existing = prompts.findTemplateByCodeAndVersion(promptCode, promptVersion).orElse(null);
        LocalDateTime now = clock.now();
        AiPromptTemplate template = AiPromptTemplate.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .promptCode(promptCode)
            .promptVersion(promptVersion)
            .scenario(scenario)
            .templateName(normalizeText(command.templateName(), "Prompt模板名称不能为空"))
            .templateContent(templateContent)
            .status(status)
            .description(trimToNull(command.description()))
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? operator.userBizId() : existing.createdBy())
            .updatedBy(operator.userBizId())
            .build();
        AiPromptTemplate saved = prompts.saveTemplate(template);
        prompts.replaceVariables(saved.bizId(), toVariables(saved.bizId(), variableCommands, now));
        prompts.replaceOutputSchemas(saved.bizId(), toOutputSchemas(saved.bizId(), schemaCommands, now));
        return assemble(saved);
    }

    /**
     * 查询 Prompt 模板详情。
     *
     * @param bizId Prompt 模板业务唯一标识
     * @return Prompt 模板详情、变量和输出 Schema
     * @throws BusinessException 当 Prompt 不存在时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public AiPromptTemplateView detail(String bizId) {
        AiPromptTemplate template = prompts.findTemplateByBizId(normalizeText(bizId, "Prompt业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Prompt模板不存在"));
        return assemble(template);
    }

    /**
     * 分页查询 Prompt 模板。
     *
     * @param promptCode Prompt 编码筛选
     * @param scenario 场景筛选
     * @param status 状态筛选
     * @param query 分页排序参数
     * @return Prompt 模板分页视图
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public PageResult<AiPromptTemplateView> list(String promptCode, String scenario, String status, PageQuery query) {
        PageResult<AiPromptTemplate> page = prompts.search(new AiPromptSearchCriteria(
            trimToNull(promptCode),
            scenario == null || scenario.isBlank() ? null : normalizeAllowed(scenario, SCENARIOS, "Prompt使用场景不合法"),
            status == null || status.isBlank() ? null : normalizeAllowed(status, STATUSES, "Prompt状态不合法"),
            query.page(),
            query.size(),
            query.safeSort(SORTS, "updatedAt"),
            "asc".equals(query.direction())
        ));
        return PageResult.<AiPromptTemplateView>builder()
            .items(page.items().stream().map(this::assemble).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 变更 Prompt 生命周期状态。
     *
     * @param bizId Prompt 模板业务唯一标识
     * @param status 目标状态
     * @return 变更后的 Prompt 模板视图
     * @throws BusinessException 当 Prompt 不存在或状态不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public AiPromptTemplateView changeStatus(String bizId, String status) {
        CurrentOperator operator = currentOperator.required();
        AiPromptTemplate existing = prompts.findTemplateByBizId(normalizeText(bizId, "Prompt业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Prompt模板不存在"));
        String resolvedStatus = normalizeAllowed(status, STATUSES, "Prompt状态不合法");
        LocalDateTime now = clock.now();
        AiPromptTemplate template = AiPromptTemplate.builder()
            .bizId(existing.bizId())
            .promptCode(existing.promptCode())
            .promptVersion(existing.promptVersion())
            .scenario(existing.scenario())
            .templateName(existing.templateName())
            .templateContent(existing.templateContent())
            .status(resolvedStatus)
            .description(existing.description())
            .createdAt(existing.createdAt())
            .updatedAt(now)
            .createdBy(existing.createdBy())
            .updatedBy(operator.userBizId())
            .build();
        return assemble(prompts.saveTemplate(template));
    }

    /**
     * 本地渲染 Prompt 预览，只做变量替换和必填校验，不触发真实大模型调用。
     *
     * @param command Prompt 预览命令
     * @return 渲染结果、缺失变量和可调用标记
     * @throws BusinessException 当 Prompt 定位条件不足或 Prompt 不存在时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public AiPromptPreviewView preview(AiPromptPreviewCommand command) {
        AiPromptTemplate template = resolvePreviewTemplate(command);
        List<AiPromptVariable> definitions = prompts.findVariables(template.bizId());
        Map<String, String> values = command.variables() == null ? Map.of() : command.variables();
        List<String> missingVariables = definitions.stream()
            .filter(AiPromptVariable::required)
            .map(AiPromptVariable::variableName)
            .filter(name -> !values.containsKey(name) || values.get(name) == null || values.get(name).isBlank())
            .toList();

        String rendered = template.templateContent();
        for (AiPromptVariable variable : definitions) {
            String value = values.get(variable.variableName());
            if (value != null) {
                rendered = rendered.replace("${" + variable.variableName() + "}", value);
            }
        }
        boolean ready = missingVariables.isEmpty() && ACTIVE_STATUS.equals(template.status());
        return AiPromptPreviewView.builder()
            .promptBizId(template.bizId())
            .promptCode(template.promptCode())
            .promptVersion(template.promptVersion())
            .scenario(template.scenario())
            .renderedPrompt(rendered)
            .missingVariables(missingVariables)
            .readyForModel(ready)
            .displayMessage(resolvePreviewMessage(template.status(), missingVariables))
            .build();
    }

    /** 组装包含变量和输出 Schema 的 Prompt 模板视图。 */
    private AiPromptTemplateView assemble(AiPromptTemplate template) {
        return AiPromptTemplateView.builder()
            .bizId(template.bizId())
            .promptCode(template.promptCode())
            .promptVersion(template.promptVersion())
            .scenario(template.scenario())
            .templateName(template.templateName())
            .templateContent(template.templateContent())
            .status(template.status())
            .description(template.description())
            .variables(prompts.findVariables(template.bizId()).stream().map(this::toVariableView).toList())
            .outputSchemas(prompts.findOutputSchemas(template.bizId()).stream().map(this::toOutputSchemaView).toList())
            .createdAt(template.createdAt())
            .updatedAt(template.updatedAt())
            .createdBy(template.createdBy())
            .updatedBy(template.updatedBy())
            .build();
    }

    /** 根据预览命令中的业务 ID 或编码版本定位模板。 */
    private AiPromptTemplate resolvePreviewTemplate(AiPromptPreviewCommand command) {
        if (command.promptBizId() != null && !command.promptBizId().isBlank()) {
            return prompts.findTemplateByBizId(command.promptBizId().trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Prompt模板不存在"));
        }
        String promptCode = normalizeCode(command.promptCode(), "Prompt编码不能为空");
        String promptVersion = normalizeText(command.promptVersion(), "Prompt版本不能为空");
        return prompts.findTemplateByCodeAndVersion(promptCode, promptVersion)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Prompt模板不存在"));
    }

    /** 将变量命令转换为领域对象。 */
    private List<AiPromptVariable> toVariables(
        String promptBizId,
        List<AiPromptVariableCommand> commands,
        LocalDateTime now
    ) {
        return commands.stream()
            .map(command -> AiPromptVariable.builder()
                .bizId(ids.newBizId())
                .promptBizId(promptBizId)
                .variableName(normalizeVariableName(command.variableName()))
                .sourcePath(trimToNull(command.sourcePath()))
                .required(command.required() == null || command.required())
                .description(trimToNull(command.description()))
                .createdAt(now)
                .build())
            .toList();
    }

    /** 将输出 Schema 命令转换为领域对象。 */
    private List<AiPromptOutputSchema> toOutputSchemas(
        String promptBizId,
        List<AiPromptOutputSchemaCommand> commands,
        LocalDateTime now
    ) {
        return commands.stream()
            .map(command -> AiPromptOutputSchema.builder()
                .bizId(ids.newBizId())
                .promptBizId(promptBizId)
                .schemaVersion(normalizeText(command.schemaVersion(), "Schema版本不能为空"))
                .schemaJson(normalizeText(command.schemaJson(), "Schema JSON不能为空"))
                .createdAt(now)
                .build())
            .toList();
    }

    /** 校验变量名格式和重复定义。 */
    private void validateVariableDefinitions(List<AiPromptVariableCommand> commands) {
        Set<String> variableNames = new HashSet<>();
        commands.stream()
            .map(AiPromptVariableCommand::variableName)
            .map(this::normalizeVariableName)
            .forEach(name -> {
                if (!variableNames.add(name)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "Prompt变量重复: " + name);
                }
            });
    }

    /** 校验输出 Schema 版本唯一且 JSON 可解析。 */
    private void validateOutputSchemas(List<AiPromptOutputSchemaCommand> commands) {
        Set<String> schemaVersions = new HashSet<>();
        commands.forEach(command -> {
            String schemaVersion = normalizeText(command.schemaVersion(), "Schema版本不能为空");
            if (!schemaVersions.add(schemaVersion)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Prompt输出Schema版本重复: " + schemaVersion);
            }
            validateJson(command.schemaJson());
        });
    }

    /** 校验 JSON 字符串格式。 */
    private void validateJson(String value) {
        String text = normalizeText(value, "Schema JSON不能为空");
        if (!Jsons.isValid(text)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Schema JSON格式不合法");
        }
    }

    /** 规范化变量名。 */
    private String normalizeVariableName(String value) {
        String normalized = normalizeText(value, "Prompt变量名不能为空");
        if (!VARIABLE_NAME_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Prompt变量名只能使用字母、数字和下划线，且必须以字母开头");
        }
        return normalized;
    }

    /** 将变量领域对象转换为应用层视图。 */
    private AiPromptVariableView toVariableView(AiPromptVariable variable) {
        return AiPromptVariableView.builder()
            .bizId(variable.bizId())
            .promptBizId(variable.promptBizId())
            .variableName(variable.variableName())
            .sourcePath(variable.sourcePath())
            .required(variable.required())
            .description(variable.description())
            .createdAt(variable.createdAt())
            .build();
    }

    /** 将输出 Schema 领域对象转换为应用层视图。 */
    private AiPromptOutputSchemaView toOutputSchemaView(AiPromptOutputSchema schema) {
        return AiPromptOutputSchemaView.builder()
            .bizId(schema.bizId())
            .promptBizId(schema.promptBizId())
            .schemaVersion(schema.schemaVersion())
            .schemaJson(schema.schemaJson())
            .createdAt(schema.createdAt())
            .build();
    }

    /** 根据状态和缺失变量生成前端可直接展示的预览提示。 */
    private String resolvePreviewMessage(String status, List<String> missingVariables) {
        if (!missingVariables.isEmpty()) {
            return "Prompt预览缺少必填变量";
        }
        if (!ACTIVE_STATUS.equals(status)) {
            return "Prompt当前不是ACTIVE状态，仅允许后台预览";
        }
        return "Prompt预览可进入后续模型调用";
    }

    /** 规范化业务编码。 */
    private String normalizeCode(String value, String message) {
        return normalizeText(value, message).toUpperCase(Locale.ROOT);
    }

    /** 将文本转换为大写并校验是否在允许集合中。 */
    private String normalizeAllowed(String value, Set<String> allowed, String message) {
        String normalized = normalizeText(value, message).toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    /** 规范化必填文本。 */
    private String normalizeText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    /** 空白文本返回默认值。 */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /** 去除首尾空白，空字符串返回 null。 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
