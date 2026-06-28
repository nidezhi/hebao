package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.command.ai.SaveAiModelBindingCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelBindingView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.model.ai.AiModelBinding;
import com.example.dzcom.domain.repository.ai.AiModelBindingSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelBindingStore;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

/** AI 模型业务场景挂靠配置应用服务。 */
@Service
@RequiredArgsConstructor
public class AiModelBindingApplicationService {
    public static final String DEFAULT_ENVIRONMENT = "DEFAULT";
    public static final String DATA_SOURCE_DISCOVERY = "DATA_SOURCE_DISCOVERY";
    private static final Set<String> SORTS = Set.of(
        "updatedAt", "scenarioCode", "modelCode", "providerCode", "environment", "enabled");

    private final AiModelBindingStore bindings;
    private final AiModelStore models;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 保存业务场景到 AI 模型的挂靠配置。
     *
     * @param command 保存命令
     * @return 保存后的挂靠配置
     * @throws BusinessException 当场景、模型或 JSON 配置不合法时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional
    public AiModelBindingView save(SaveAiModelBindingCommand command) {
        CurrentOperator operator = currentOperator.required();
        String scenarioCode = normalizeCode(command.scenarioCode(), "场景编码不能为空");
        String environment = normalizeEnvironment(command.environment());
        String modelCode = normalizeModelCode(command.modelCode(), "模型编码不能为空");
        validateJson(command.config());
        models.findActiveByCode(modelCode)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "挂靠模型不存在或未启用: " + modelCode));
        AiModelBinding existing = bindings.findByScenarioAndEnvironment(scenarioCode, environment).orElse(null);
        LocalDateTime now = clock.now();
        AiModelBinding binding = AiModelBinding.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .scenarioCode(scenarioCode)
            .scenarioName(normalizeText(command.scenarioName(), "场景名称不能为空"))
            .modelCode(modelCode)
            .providerCode(trimToNull(command.providerCode()))
            .environment(environment)
            .enabled(command.enabled() == null || command.enabled())
            .config(trimToNull(command.config()))
            .description(trimToNull(command.description()))
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? operator.userBizId() : existing.createdBy())
            .updatedBy(operator.userBizId())
            .build();
        return toView(bindings.save(binding));
    }

    /**
     * 查询指定场景和环境的模型挂靠配置。
     *
     * @param scenarioCode 场景编码
     * @param environment 环境编码
     * @return 模型挂靠配置
     * @throws BusinessException 当配置不存在时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public AiModelBindingView detail(String scenarioCode, String environment) {
        return toView(requiredBinding(scenarioCode, environment));
    }

    /**
     * 查询启用状态的场景模型挂靠配置。
     *
     * @param scenarioCode 场景编码
     * @param environment 环境编码
     * @return 启用的模型挂靠配置
     * @throws BusinessException 当配置不存在或未启用时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public AiModelBinding enabledBinding(String scenarioCode, String environment) {
        AiModelBinding binding = requiredBinding(scenarioCode, environment);
        if (!binding.enabled()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "模型挂靠配置未启用: " + binding.scenarioCode());
        }
        return binding;
    }

    /**
     * 分页查询模型挂靠配置。
     *
     * @param scenarioCode 场景编码
     * @param modelCode 模型编码
     * @param providerCode 提供方编码
     * @param environment 环境编码
     * @param enabled 是否启用
     * @param query 分页排序
     * @return 分页视图
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public PageResult<AiModelBindingView> list(
        String scenarioCode,
        String modelCode,
        String providerCode,
        String environment,
        Boolean enabled,
        PageQuery query
    ) {
        PageResult<AiModelBinding> page = bindings.search(new AiModelBindingSearchCriteria(
            trimToNull(scenarioCode) == null ? null : normalizeCode(scenarioCode, "场景编码不能为空"),
            trimToNull(modelCode) == null ? null : normalizeModelCode(modelCode, "模型编码不能为空"),
            trimToNull(providerCode),
            trimToNull(environment) == null ? null : normalizeEnvironment(environment),
            enabled,
            query.page(),
            query.size(),
            query.safeSort(SORTS, "updatedAt"),
            "asc".equalsIgnoreCase(query.direction())
        ));
        return PageResult.<AiModelBindingView>builder()
            .items(page.items().stream().map(this::toView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 查询指定场景和环境的模型挂靠配置。
     *
     * @param scenarioCode 场景编码
     * @param environment 环境编码
     * @return 模型挂靠配置
     * @throws BusinessException 当配置不存在时抛出
     * @author dz
     * @date 2026-06-26
     */
    private AiModelBinding requiredBinding(String scenarioCode, String environment) {
        String safeScenarioCode = normalizeCode(scenarioCode, "场景编码不能为空");
        String safeEnvironment = normalizeEnvironment(environment);
        return bindings.findByScenarioAndEnvironment(safeScenarioCode, safeEnvironment)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模型挂靠配置不存在"));
    }

    /** 将领域对象转换为应用层视图。 */
    private AiModelBindingView toView(AiModelBinding binding) {
        return AiModelBindingView.builder()
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
            .build();
    }

    /** 标准化编码类字段。 */
    private String normalizeCode(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    /** 标准化模型编码，保留模型注册时的大小写语义。 */
    private String normalizeModelCode(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    /** 标准化环境编码。 */
    private String normalizeEnvironment(String environment) {
        return environment == null || environment.isBlank()
            ? DEFAULT_ENVIRONMENT
            : environment.trim().toUpperCase(Locale.ROOT);
    }

    /** 标准化必填文本。 */
    private String normalizeText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    /** 空白文本转换为空值。 */
    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 校验 JSON 字符串。 */
    private void validateJson(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!Jsons.isValid(value)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "模型挂靠场景配置JSON不合法");
        }
    }
}
