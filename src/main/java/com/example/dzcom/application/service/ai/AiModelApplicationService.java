package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelSkillBindingView;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.AiModelSkillBinding;
import com.example.dzcom.domain.repository.ai.AiModelSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingStore;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** AI 模型注册、配置和状态管理用例。 */
@Service
@RequiredArgsConstructor
public class AiModelApplicationService {
    private static final String DEFAULT_STATUS = "DRAFT";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String INACTIVE_STATUS = "INACTIVE";
    private static final String ARCHIVED_STATUS = "ARCHIVED";
    private static final Set<String> MODEL_STATUSES =
        Set.of("DRAFT", "VALIDATING", ACTIVE_STATUS, INACTIVE_STATUS, ARCHIVED_STATUS);
    private static final Set<String> SORTS = Set.of(
        "updatedAt", "modelCode", "modelVersion", "modelType", "provider", "status", "activatedAt");

    private final AiModelStore models;
    private final AiModelSkillBindingStore modelSkillBindings;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 按模型编码和版本新增或更新模型配置。
     *
     * <p>模型编码与版本共同确定一个稳定版本。首次保存时生成业务 ID 和创建时间，
     * 后续保存保留原创建时间，并根据生命周期状态维护启用和停用时间。</p>
     *
     * @param modelCode 模型跨版本稳定编码
     * @param modelVersion 当前模型版本号
     * @param modelName 管理端展示名称
     * @param modelType 模型业务类型
     * @param provider 模型提供方或运行平台
     * @param artifactUri 模型制品、提示词或配置地址
     * @param modelConfig 脱敏后的模型配置 JSON
     * @param metrics 离线评估指标 JSON
     * @param status 目标生命周期状态，为空时使用 DRAFT
     * @return 保存后的完整模型领域对象
     * @throws BusinessException 当模型状态不在允许集合中时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Transactional
    public AiModel save(String modelCode, String modelVersion, String modelName, String modelType,
                        String provider, String artifactUri, String modelConfig, String metrics,
                        String status) {
        LocalDateTime now = clock.now();
        AiModel existingModel = models.findByCodeAndVersion(modelCode, modelVersion).orElse(null);
        String resolvedStatus = resolveStatus(status);
        LocalDateTime activatedAt = resolveActivatedAt(existingModel, resolvedStatus, now);
        LocalDateTime retiredAt = resolveRetiredAt(existingModel, resolvedStatus, now);

        AiModel model = AiModel.builder()
            .bizId(existingModel == null ? ids.newBizId() : existingModel.bizId())
            .modelCode(modelCode)
            .modelVersion(modelVersion)
            .modelName(modelName)
            .modelType(modelType)
            .provider(provider)
            .artifactUri(artifactUri)
            .modelConfig(modelConfig)
            .metrics(metrics)
            .status(resolvedStatus)
            .activatedAt(activatedAt)
            .retiredAt(retiredAt)
            .createdAt(existingModel == null ? now : existingModel.createdAt())
            .updatedAt(now)
            .build();
        return models.save(model);
    }

    /**
     * 根据模型业务 ID 查询完整模型配置。
     *
     * @param bizId 模型业务唯一标识
     * @return 模型版本、配置、指标和生命周期信息
     * @throws BusinessException 当模型不存在时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Transactional(readOnly = true)
    public AiModel detail(String bizId) {
        return models.findByBizId(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "AI模型不存在"));
    }

    /**
     * 按模型编码、类型、提供方和状态分页查询模型。
     *
     * @param modelCode 模型编码筛选条件
     * @param modelType 模型业务类型筛选条件
     * @param provider 模型提供方筛选条件
     * @param status 生命周期状态筛选条件
     * @param pageQuery 分页、排序和排序方向
     * @return 满足筛选条件的模型分页结果
     * @author dz
     * @date 2026-06-18
     */
    @Transactional(readOnly = true)
    public PageResult<AiModel> list(String modelCode, String modelType, String provider,
                                    String status, PageQuery pageQuery) {
        return models.search(new AiModelSearchCriteria(
            modelCode,
            modelType,
            provider,
            status,
            pageQuery.page(),
            pageQuery.size(),
            pageQuery.safeSort(SORTS, "updatedAt"),
            "asc".equals(pageQuery.direction())
        ));
    }

    /**
     * 变更模型生命周期状态并维护启用、停用时间。
     *
     * @param bizId 模型业务唯一标识
     * @param status 目标生命周期状态
     * @return 状态变更后的模型
     * @throws BusinessException 当模型不存在或状态不合法时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Transactional
    public AiModel changeStatus(String bizId, String status) {
        AiModel existingModel = detail(bizId);
        LocalDateTime now = clock.now();
        String resolvedStatus = resolveStatus(status);

        AiModel model = AiModel.builder()
            .bizId(existingModel.bizId())
            .modelCode(existingModel.modelCode())
            .modelVersion(existingModel.modelVersion())
            .modelName(existingModel.modelName())
            .modelType(existingModel.modelType())
            .provider(existingModel.provider())
            .artifactUri(existingModel.artifactUri())
            .modelConfig(existingModel.modelConfig())
            .metrics(existingModel.metrics())
            .status(resolvedStatus)
            .activatedAt(resolveActivatedAt(existingModel, resolvedStatus, now))
            .retiredAt(resolveRetiredAt(existingModel, resolvedStatus, now))
            .createdAt(existingModel.createdAt())
            .updatedAt(now)
            .build();
        return models.save(model);
    }

    /**
     * 归档模型，保留历史配置和报告引用，不执行物理删除。
     *
     * @param bizId 模型业务唯一标识
     * @return 状态为 ARCHIVED 的模型
     * @throws BusinessException 当模型不存在时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Transactional
    public AiModel archive(String bizId) {
        return changeStatus(bizId, ARCHIVED_STATUS);
    }

    /**
     * 查询指定模型实例当前启用的 Skill 绑定。
     *
     * @param modelBizId 模型业务唯一标识
     * @return 模型启用的 Skill 绑定视图
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public List<AiModelSkillBindingView> enabledSkills(String modelBizId) {
        return modelSkillBindings.findEnabledByModelBizId(modelBizId).stream()
            .map(this::toSkillBindingView)
            .toList();
    }

    /** 将模型 Skill 绑定领域对象转换为应用层视图。 */
    private AiModelSkillBindingView toSkillBindingView(AiModelSkillBinding binding) {
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

    /**
     * 规范化并校验模型生命周期状态。
     *
     * @param status 外部传入状态
     * @return 大写形式的合法状态；空值返回 DRAFT
     * @throws BusinessException 当状态不在允许集合中时抛出
     * @author dz
     * @date 2026-06-18
     */
    private String resolveStatus(String status) {
        String resolvedStatus = status == null || status.isBlank()
            ? DEFAULT_STATUS
            : status.trim().toUpperCase(Locale.ROOT);
        if (!MODEL_STATUSES.contains(resolvedStatus)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI模型状态不合法");
        }
        return resolvedStatus;
    }

    /**
     * 根据目标状态计算模型启用时间。
     *
     * @param existingModel 已存在模型，首次创建时为空
     * @param status 目标生命周期状态
     * @param now 当前北京时间
     * @return 首次进入 ACTIVE 的时间；未启用时为空
     * @author dz
     * @date 2026-06-18
     */
    private LocalDateTime resolveActivatedAt(
        AiModel existingModel,
        String status,
        LocalDateTime now
    ) {
        if (!ACTIVE_STATUS.equals(status)) {
            return existingModel == null ? null : existingModel.activatedAt();
        }
        if (existingModel == null || existingModel.activatedAt() == null) {
            return now;
        }
        return existingModel.activatedAt();
    }

    /**
     * 根据目标状态计算模型停用时间。
     *
     * @param existingModel 已存在模型，首次创建时为空
     * @param status 目标生命周期状态
     * @param now 当前北京时间
     * @return 进入 INACTIVE 或 ARCHIVED 的时间；重新启用时为空
     * @author dz
     * @date 2026-06-18
     */
    private LocalDateTime resolveRetiredAt(
        AiModel existingModel,
        String status,
        LocalDateTime now
    ) {
        if (INACTIVE_STATUS.equals(status) || ARCHIVED_STATUS.equals(status)) {
            return now;
        }
        if (ACTIVE_STATUS.equals(status)) {
            return null;
        }
        return existingModel == null ? null : existingModel.retiredAt();
    }
}
