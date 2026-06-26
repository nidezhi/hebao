package com.example.dzcom.application.service.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.example.dzcom.application.command.ai.SaveAiSkillCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiSkillView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.model.ai.AiSkill;
import com.example.dzcom.domain.repository.ai.AiSkillSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiSkillStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

/** AI Skill 版本、指令、Schema 和生命周期管理应用服务。 */
@Service
@RequiredArgsConstructor
public class AiSkillApplicationService {
    private static final String DEFAULT_STATUS = "DRAFT";
    private static final Set<String> STATUSES = Set.of("DRAFT", "VALIDATING", "ACTIVE", "RETIRED", "ARCHIVED");
    private static final Set<String> SKILL_TYPES = Set.of(
        "DATA_SOURCE_DISCOVERY", "PROMPT_GOVERNANCE", "REPORT_ANALYSIS", "QUALITY_AUDIT", "MODEL_FEEDBACK");
    private static final Set<String> SORTS = Set.of(
        "updatedAt", "skillCode", "skillVersion", "skillType", "status");

    private final AiSkillStore skills;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 新增或更新 AI Skill 版本。
     *
     * @param command Skill 保存命令
     * @return 保存后的 Skill 视图
     * @throws BusinessException 当编码、类型、状态或 JSON Schema 不合法时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional
    public AiSkillView save(SaveAiSkillCommand command) {
        CurrentOperator operator = currentOperator.required();
        String skillCode = normalizeCode(command.skillCode(), "Skill编码不能为空");
        String skillVersion = normalizeText(command.skillVersion(), "Skill版本不能为空");
        String skillType = normalizeAllowed(command.skillType(), SKILL_TYPES, "Skill类型不合法");
        String status = normalizeAllowed(defaultIfBlank(command.status(), DEFAULT_STATUS), STATUSES, "Skill状态不合法");
        validateJson(command.inputSchema(), "输入Schema JSON不合法");
        validateJson(command.outputSchema(), "输出Schema JSON不合法");
        validateJson(command.evaluationPolicy(), "评估策略JSON不合法");
        AiSkill existing = skills.findByCodeAndVersion(skillCode, skillVersion).orElse(null);
        LocalDateTime now = clock.now();
        AiSkill skill = AiSkill.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .skillCode(skillCode)
            .skillVersion(skillVersion)
            .skillName(normalizeText(command.skillName(), "Skill名称不能为空"))
            .skillType(skillType)
            .status(status)
            .instructionContent(normalizeText(command.instructionContent(), "Skill指令内容不能为空"))
            .inputSchema(trimToNull(command.inputSchema()))
            .outputSchema(trimToNull(command.outputSchema()))
            .evaluationPolicy(trimToNull(command.evaluationPolicy()))
            .description(trimToNull(command.description()))
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? operator.userBizId() : existing.createdBy())
            .updatedBy(operator.userBizId())
            .build();
        return toView(skills.save(skill));
    }

    /**
     * 查询 AI Skill 详情。
     *
     * @param bizId Skill 业务 ID
     * @return Skill 详情
     * @throws BusinessException 当 Skill 不存在时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public AiSkillView detail(String bizId) {
        return toView(requiredSkill(bizId));
    }

    /**
     * 变更 AI Skill 生命周期状态。
     *
     * @param bizId Skill 业务 ID
     * @param status 目标状态
     * @return 变更后的 Skill
     * @throws BusinessException 当 Skill 不存在或状态不合法时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional
    public AiSkillView changeStatus(String bizId, String status) {
        CurrentOperator operator = currentOperator.required();
        AiSkill existing = requiredSkill(bizId);
        LocalDateTime now = clock.now();
        AiSkill skill = existing.toBuilder()
            .status(normalizeAllowed(status, STATUSES, "Skill状态不合法"))
            .updatedAt(now)
            .updatedBy(operator.userBizId())
            .build();
        return toView(skills.save(skill));
    }

    /**
     * 分页查询 AI Skill。
     *
     * @param skillCode Skill 编码
     * @param skillType Skill 类型
     * @param status 状态
     * @param keyword 关键词
     * @param query 分页排序参数
     * @return Skill 分页视图
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public PageResult<AiSkillView> list(
        String skillCode,
        String skillType,
        String status,
        String keyword,
        PageQuery query
    ) {
        PageResult<AiSkill> page = skills.search(new AiSkillSearchCriteria(
            trimToNull(skillCode) == null ? null : normalizeCode(skillCode, "Skill编码不能为空"),
            trimToNull(skillType) == null ? null : normalizeAllowed(skillType, SKILL_TYPES, "Skill类型不合法"),
            trimToNull(status) == null ? null : normalizeAllowed(status, STATUSES, "Skill状态不合法"),
            trimToNull(keyword),
            query.page(),
            query.size(),
            query.safeSort(SORTS, "updatedAt"),
            "asc".equalsIgnoreCase(query.direction())
        ));
        return PageResult.<AiSkillView>builder()
            .items(page.items().stream().map(this::toView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 查询最近启用的 Skill 版本。
     *
     * @param skillCode Skill 编码
     * @return 启用的 Skill 领域对象
     * @throws BusinessException 当 Skill 不存在或未启用时抛出
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public AiSkill enabledSkill(String skillCode) {
        String safeCode = normalizeCode(skillCode, "Skill编码不能为空");
        return skills.findActiveByCode(safeCode)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "启用的Skill不存在: " + safeCode));
    }

    /** 查询必需存在的 Skill。 */
    private AiSkill requiredSkill(String bizId) {
        return skills.findByBizId(normalizeText(bizId, "Skill业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "AI Skill不存在"));
    }

    /** 将领域对象转换为应用层视图。 */
    private AiSkillView toView(AiSkill skill) {
        return AiSkillView.builder()
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
            .build();
    }

    /** 标准化编码类字段。 */
    private String normalizeCode(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    /** 标准化枚举字段。 */
    private String normalizeAllowed(String value, Set<String> allowed, String message) {
        String normalized = normalizeCode(value, message);
        if (!allowed.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
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

    /** 返回非空默认文本。 */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /** 校验 JSON 字符串。 */
    private void validateJson(String value, String message) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            JSON.parse(value);
        } catch (JSONException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
