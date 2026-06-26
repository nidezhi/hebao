package com.example.dzcom.application.service.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.example.dzcom.application.command.ai.SaveAiModelSkillBindingCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelSkillBindingView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.AiModelSkillBinding;
import com.example.dzcom.domain.model.ai.AiSkill;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingStore;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.domain.repository.ai.AiSkillStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** AI 模型实例与 Skill 版本绑定应用服务。 */
@Service
@RequiredArgsConstructor
public class AiModelSkillBindingApplicationService {
    private static final Set<String> SORTS = Set.of(
        "updatedAt", "modelCode", "skillCode", "scenarioCode", "priority", "enabled");

    private final AiModelSkillBindingStore bindings;
    private final AiModelStore models;
    private final AiSkillStore skills;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 保存模型实例与 Skill 版本绑定。
     *
     * @param command 保存绑定命令
     * @return 保存后的绑定视图
     * @throws BusinessException 当模型、Skill 或绑定配置不合法时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional
    public AiModelSkillBindingView save(SaveAiModelSkillBindingCommand command) {
        CurrentOperator operator = currentOperator.required();
        AiModel model = models.findByBizId(normalizeText(command.modelBizId(), "模型业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "AI模型不存在"));
        AiSkill skill = skills.findByBizId(normalizeText(command.skillBizId(), "Skill业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "AI Skill不存在"));
        String scenarioCode = normalizeCode(command.scenarioCode(), "业务场景编码不能为空");
        validateJson(command.config());
        AiModelSkillBinding existing = bindings
            .findByModelSkillAndScenario(model.bizId(), skill.bizId(), scenarioCode)
            .orElse(null);
        LocalDateTime now = clock.now();
        AiModelSkillBinding binding = AiModelSkillBinding.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .modelBizId(model.bizId())
            .modelCode(model.modelCode())
            .modelVersion(model.modelVersion())
            .skillBizId(skill.bizId())
            .skillCode(skill.skillCode())
            .skillVersion(skill.skillVersion())
            .scenarioCode(scenarioCode)
            .priority(command.priority() == null ? 100 : Math.max(1, command.priority()))
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
     * 查询模型 Skill 绑定详情。
     *
     * @param bizId 绑定业务 ID
     * @return 绑定详情
     * @throws BusinessException 当绑定不存在时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public AiModelSkillBindingView detail(String bizId) {
        return toView(bindings.findByBizId(normalizeText(bizId, "绑定业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模型Skill绑定不存在")));
    }

    /**
     * 查询模型下启用的 Skill 绑定。
     *
     * @param modelBizId 模型业务 ID
     * @return 启用的模型 Skill 绑定列表
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public List<AiModelSkillBindingView> enabledByModel(String modelBizId) {
        String safeModelBizId = normalizeText(modelBizId, "模型业务ID不能为空");
        return bindings.findEnabledByModelBizId(safeModelBizId).stream().map(this::toView).toList();
    }

    /**
     * 分页查询模型 Skill 绑定。
     *
     * @param modelBizId 模型业务 ID
     * @param modelCode 模型编码
     * @param skillCode Skill 编码
     * @param scenarioCode 场景编码
     * @param enabled 是否启用
     * @param query 分页排序参数
     * @return 绑定分页视图
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public PageResult<AiModelSkillBindingView> list(
        String modelBizId,
        String modelCode,
        String skillCode,
        String scenarioCode,
        Boolean enabled,
        PageQuery query
    ) {
        PageResult<AiModelSkillBinding> page = bindings.search(new AiModelSkillBindingSearchCriteria(
            trimToNull(modelBizId),
            trimToNull(modelCode),
            trimToNull(skillCode) == null ? null : normalizeCode(skillCode, "Skill编码不能为空"),
            trimToNull(scenarioCode) == null ? null : normalizeCode(scenarioCode, "业务场景编码不能为空"),
            enabled,
            query.page(),
            query.size(),
            query.safeSort(SORTS, "updatedAt"),
            "asc".equalsIgnoreCase(query.direction())
        ));
        return PageResult.<AiModelSkillBindingView>builder()
            .items(page.items().stream().map(this::toView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /** 将领域对象转换为应用层视图。 */
    private AiModelSkillBindingView toView(AiModelSkillBinding binding) {
        return AiModelSkillBindingView.builder()
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
            .build();
    }

    /** 标准化编码类字段。 */
    private String normalizeCode(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim().toUpperCase(Locale.ROOT);
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

    /** 校验绑定配置 JSON。 */
    private void validateJson(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            JSON.parse(value);
        } catch (JSONException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "模型Skill绑定配置JSON不合法");
        }
    }
}
