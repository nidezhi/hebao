package com.example.dzcom.application.service.market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.dzcom.application.command.market.DiscoverDataSourcesCommand;
import com.example.dzcom.application.command.market.SaveDataQualitySnapshotCommand;
import com.example.dzcom.application.command.market.SaveDataSourceCommand;
import com.example.dzcom.application.command.market.SaveDataSourceHealthCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.market.DataQualitySnapshotView;
import com.example.dzcom.application.dto.market.DataSourceDiscoveryCandidateView;
import com.example.dzcom.application.dto.market.DataSourceDiscoveryView;
import com.example.dzcom.application.dto.market.DataSourceHealthView;
import com.example.dzcom.application.dto.market.DataSourceView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.ai.AiJsonCompletionClient;
import com.example.dzcom.application.service.ai.AiModelBindingApplicationService;
import com.example.dzcom.application.service.ai.AiModelRuntimeConfigResolver;
import com.example.dzcom.domain.model.ai.AiModelBinding;
import com.example.dzcom.domain.model.ai.AiSkill;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.domain.repository.ai.AiSkillStore;
import com.example.dzcom.domain.repository.market.DataSourceSearchCriteria;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 数据源治理应用服务。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSourceGovernanceApplicationService {
    private static final int SOURCE_CODE_MAX_LENGTH = 64;
    private static final int SOURCE_NAME_MAX_LENGTH = 128;
    private static final int BASE_URL_MAX_LENGTH = 512;
    private static final int FETCH_FREQUENCY_MAX_LENGTH = 255;
    private static final int OWNER_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 512;
    private static final Set<String> SOURCE_TYPES =
        Set.of("MARKET", "NEWS", "ANNOUNCEMENT", "RESEARCH", "REGULATORY", "FALLBACK");
    private static final Set<String> TRUST_LEVELS = Set.of("L1", "L2", "L3", "L4", "L5");
    private static final Set<String> DATA_TYPES =
        Set.of("MARKET_QUOTE", "NEWS", "ANNOUNCEMENT", "RESEARCH", "REGULATORY");
    private static final Map<String, String> DATA_TYPE_SOURCE_TYPES = Map.of(
        "MARKET_QUOTE", "MARKET",
        "NEWS", "NEWS",
        "ANNOUNCEMENT", "ANNOUNCEMENT",
        "RESEARCH", "RESEARCH",
        "REGULATORY", "REGULATORY"
    );
    private static final Set<String> SORT_FIELDS =
        Set.of("updatedAt", "sourceCode", "sourceName", "sourceType", "trustLevel", "enabled");
    private static final String CN_MAINLAND = "CN_MAINLAND";
    private static final String DEFAULT_DISCOVERY_SKILL = "DATA_COLLECTION_MULTI_SOURCE";

    private final DataSourceStore sources;
    private final AiModelBindingApplicationService modelBindings;
    private final AiModelStore models;
    private final AiSkillStore skills;
    private final AiModelRuntimeConfigResolver modelRuntimeConfigs;
    private final List<AiJsonCompletionClient> aiJsonClients;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 保存数据源注册信息。
     *
     * @param command 数据源注册命令
     * @return 数据源治理视图
     * @throws BusinessException 当编码、类型或来源等级不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public DataSourceView save(SaveDataSourceCommand command) {
        CurrentOperator operator = currentOperator.required();
        return saveInternal(command, operator, command.enabled() == null || command.enabled());
    }

    /**
     * 沉淀 AI 数据源发现候选。
     *
     * <p>候选保存遵循“显式开关才启用”的原则：未开启自动启用时只沉淀候选；
     * 开启自动启用时，允许把此前处于 disabled 的同编码候选提升为 enabled，
     * 避免闭环长期停在候选态。</p>
     *
     * @param command 数据源候选保存命令
     * @param autoEnableCandidate 是否允许自动启用新候选
     * @return 数据源治理视图
     * @author dz
     * @date 2026-06-27
     */
    @Transactional
    public DataSourceView saveDiscoveredCandidate(SaveDataSourceCommand command, boolean autoEnableCandidate) {
        CurrentOperator operator = currentOperator.required();
        String sourceCode = normalizeCode(command.sourceCode(), "数据源编码不能为空");
        DataSource existing = sources.findBySourceCode(sourceCode).orElse(null);
        boolean enabled = existing == null ? autoEnableCandidate : existing.enabled() || autoEnableCandidate;
        return saveInternal(command.toBuilder().sourceCode(sourceCode).enabled(enabled).build(), operator, enabled);
    }

    /** 保存数据源注册信息的内部实现。 */
    private DataSourceView saveInternal(SaveDataSourceCommand command, CurrentOperator operator, boolean enabled) {
        String sourceCode = normalizeCode(command.sourceCode(), "数据源编码不能为空");
        String sourceType = normalizeAllowed(command.sourceType(), SOURCE_TYPES, "数据源类型不合法");
        String trustLevel = normalizeAllowed(command.trustLevel(), TRUST_LEVELS, "来源等级不合法");
        DataSource existing = sources.findBySourceCode(sourceCode).orElse(null);
        LocalDateTime now = clock.now();
        DataSource source = DataSource.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .sourceName(limitText(normalizeText(command.sourceName(), "数据源名称不能为空"), SOURCE_NAME_MAX_LENGTH))
            .sourceType(sourceType)
            .trustLevel(trustLevel)
            .baseUrl(limitText(trimToNull(command.baseUrl()), BASE_URL_MAX_LENGTH))
            .enabled(enabled)
            .fetchFrequency(normalizeFetchFrequency(command.fetchFrequency()))
            .owner(normalizeOwner(command.owner()))
            .description(limitText(trimToNull(command.description()), DESCRIPTION_MAX_LENGTH))
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? operator.userBizId() : existing.createdBy())
            .updatedBy(operator.userBizId())
            .build();
        DataSource saved = sources.save(source);
        return assemble(saved);
    }

    /**
     * 保存数据源健康状态。
     *
     * @param command 数据源健康状态命令
     * @return 数据源治理视图
     * @throws BusinessException 当数据源不存在或数值不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public DataSourceView saveHealth(SaveDataSourceHealthCommand command) {
        DataSource source = requiredSource(command.sourceCode());
        DataSourceHealth existing = sources.findHealthBySourceCode(source.sourceCode()).orElse(null);
        LocalDateTime now = clock.now();
        DataSourceHealth health = DataSourceHealth.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(source.sourceCode())
            .lastSuccessAt(command.lastSuccessAt())
            .lastFailureAt(command.lastFailureAt())
            .successRate(normalizeRatio(command.successRate(), "成功率"))
            .avgLatencyMs(command.avgLatencyMs())
            .failureReason(trimToNull(command.failureReason()))
            .sampleCount(command.sampleCount() == null ? 0 : Math.max(command.sampleCount(), 0))
            .updatedAt(now)
            .build();
        sources.saveHealth(health);
        return assemble(source);
    }

    /**
     * 保存数据质量快照。
     *
     * @param command 数据质量快照命令
     * @return 保存后的数据质量快照视图
     * @throws BusinessException 当数据源、数据类型、分数或详情 JSON 不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public DataQualitySnapshotView saveQualitySnapshot(SaveDataQualitySnapshotCommand command) {
        DataSource source = requiredSource(command.sourceCode());
        String dataType = normalizeAllowed(command.dataType(), DATA_TYPES, "数据质量类型不合法");
        validateJson(command.detail());
        LocalDateTime now = clock.now();
        DataQualitySnapshot snapshot = DataQualitySnapshot.builder()
            .bizId(ids.newBizId())
            .sourceCode(source.sourceCode())
            .dataType(dataType)
            .qualityScore(normalizeRatio(command.qualityScore(), "质量分"))
            .missingRate(normalizeRatio(command.missingRate(), "缺失率"))
            .duplicateRate(normalizeRatio(command.duplicateRate(), "重复率"))
            .freshnessScore(normalizeRatio(command.freshnessScore(), "新鲜度分"))
            .sampleCount(command.sampleCount() == null ? 0 : Math.max(command.sampleCount(), 0))
            .snapshotTime(command.snapshotTime() == null ? now : command.snapshotTime())
            .detail(trimToNull(command.detail()))
            .createdAt(now)
            .build();
        return toQualityView(sources.saveQualitySnapshot(snapshot));
    }

    /**
     * 分页查询数据源看板。
     *
     * @param keyword 关键字
     * @param sourceType 数据源类型
     * @param trustLevel 来源等级
     * @param enabled 是否启用
     * @param query 分页排序参数
     * @return 数据源分页视图
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public PageResult<DataSourceView> list(String keyword, String sourceType, String trustLevel,
                                           Boolean enabled, PageQuery query) {
        PageResult<DataSource> page = sources.search(new DataSourceSearchCriteria(
            trimToNull(keyword),
            sourceType == null || sourceType.isBlank() ? null : normalizeAllowed(sourceType, SOURCE_TYPES, "数据源类型不合法"),
            trustLevel == null || trustLevel.isBlank() ? null : normalizeAllowed(trustLevel, TRUST_LEVELS, "来源等级不合法"),
            enabled,
            query.page(),
            query.size(),
            query.safeSort(SORT_FIELDS, "updatedAt"),
            "asc".equalsIgnoreCase(query.direction())
        ));
        return PageResult.<DataSourceView>builder()
            .items(page.items().stream().map(this::assemble).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 查询数据源质量快照历史。
     *
     * @param sourceCode 数据源编码
     * @param dataType 数据类型
     * @param limit 返回数量上限
     * @return 质量快照集合
     * @throws BusinessException 当数据源不存在或数量不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public List<DataQualitySnapshotView> qualitySnapshots(String sourceCode, String dataType, Integer limit) {
        DataSource source = requiredSource(sourceCode);
        int safeLimit = normalizeLimit(limit);
        String safeDataType = dataType == null || dataType.isBlank()
            ? null
            : normalizeAllowed(dataType, DATA_TYPES, "数据质量类型不合法");
        return sources.findQualitySnapshots(source.sourceCode(), safeDataType, safeLimit).stream()
            .map(this::toQualityView)
            .toList();
    }

    /**
     * 使用 AI 模型挂靠配置生成数据源候选建议。
     *
     * <p>该方法只输出候选，不直接写入正式数据源。前端需要展示候选来源、字段映射、
     * 置信度和人工审核要求，再由运营确认后调用保存数据源和任务配置接口。</p>
     *
     * @param command 数据源发现命令
     * @return 数据源发现候选结果
     * @author dz
     * @date 2026-06-26
     */
    @Transactional(readOnly = true)
    public DataSourceDiscoveryView discover(DiscoverDataSourcesCommand command) {
        log.info(
            "AI数据源发现开始: environment={}, marketScope={}, assetClass={}, dataTypes={}, collectionDirection={}, skillCode={}, preferredTrustLevels={}, candidateLimit={}",
            command.environment(),
            command.marketScope(),
            command.assetClass(),
            command.dataTypes(),
            command.collectionDirection(),
            command.skillCode(),
            command.preferredTrustLevels(),
            command.candidateLimit()
        );
        AiModelBinding binding = modelBindings.enabledBinding(
            AiModelBindingApplicationService.DATA_SOURCE_DISCOVERY,
            command.environment()
        );
        AiSkill skill = resolveDiscoverySkill(command);
        AiModel model = models.findActiveByCode(binding.modelCode())
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "数据源发现模型不存在或未启用: " + binding.modelCode()));
        int limit = normalizeDiscoveryLimit(command.candidateLimit(), binding.config());
        List<String> dataTypes = discoveryDataTypes(command.dataTypes());
        List<String> trustLevels = discoveryTrustLevels(command.preferredTrustLevels());
        String promptPreview = discoveryPromptPreview(command, binding, skill, dataTypes, trustLevels, limit);
        List<DataSourceDiscoveryCandidateView> candidates = discoverByModel(command, model, skill, dataTypes, trustLevels, limit, promptPreview)
            .stream()
            .filter(candidate -> trustLevels.isEmpty() || trustLevels.contains(candidate.trustLevel()))
            .filter(candidate -> Boolean.TRUE.equals(command.includeDisabledCandidates())
                || !"L3".equals(candidate.trustLevel()))
            .limit(limit)
            .toList();
        log.info(
            "AI数据源发现完成: scenarioCode={}, modelCode={}, providerCode={}, skillCode={}, skillVersion={}, candidateCount={}, candidateCodes={}",
            binding.scenarioCode(),
            binding.modelCode(),
            binding.providerCode(),
            skill.skillCode(),
            skill.skillVersion(),
            candidates.size(),
            candidates.stream().map(DataSourceDiscoveryCandidateView::sourceCode).toList()
        );
        return DataSourceDiscoveryView.builder()
            .scenarioCode(binding.scenarioCode())
            .modelCode(binding.modelCode())
            .providerCode(binding.providerCode())
            .environment(binding.environment())
            .marketScope(defaultText(command.marketScope(), CN_MAINLAND))
            .assetClass(defaultText(command.assetClass(), "MULTI_ASSET"))
            .dataTypes(String.join(",", dataTypes))
            .topicKeywords(defaultText(command.topicKeywords(), ""))
            .collectionDirection(defaultText(command.collectionDirection(), "MULTI_SOURCE"))
            .modelBindingConfig(new LinkedHashMap<>(parseObject(binding.config())))
            .skillCode(skill.skillCode())
            .skillVersion(skill.skillVersion())
            .skillInstruction(skill.instructionContent())
            .candidates(candidates)
            .reviewPolicy("大模型负责整理收集数据源、采集计划和字段映射；正式启用、授权供应商和真实交易仍需前端确认或灰度开关。")
            .promptPreview(promptPreview)
            .build();
    }

    /** 组装数据源治理视图。 */
    private DataSourceView assemble(DataSource source) {
        DataSourceHealthView health = sources.findHealthBySourceCode(source.sourceCode())
            .map(this::toHealthView)
            .orElse(null);
        DataQualitySnapshotView latestQuality = sources.findQualitySnapshots(source.sourceCode(), null, 1)
            .stream()
            .findFirst()
            .map(this::toQualityView)
            .orElse(null);
        String qualityLevel = resolveQualityLevel(source, health, latestQuality);
        return DataSourceView.builder()
            .bizId(source.bizId())
            .sourceCode(source.sourceCode())
            .sourceName(source.sourceName())
            .sourceType(source.sourceType())
            .trustLevel(source.trustLevel())
            .baseUrl(source.baseUrl())
            .enabled(source.enabled())
            .fetchFrequency(source.fetchFrequency())
            .owner(source.owner())
            .description(source.description())
            .health(health)
            .latestQuality(latestQuality)
            .qualityLevel(qualityLevel)
            .displayMessage(resolveDisplayMessage(source, health, latestQuality, qualityLevel))
            .createdAt(source.createdAt())
            .updatedAt(source.updatedAt())
            .build();
    }

    /** 根据质量和健康状态计算前端展示等级。 */
    private String resolveQualityLevel(
        DataSource source,
        DataSourceHealthView health,
        DataQualitySnapshotView quality
    ) {
        if (!source.enabled()) {
            return "DISABLED";
        }
        if ("L5".equals(source.trustLevel())) {
            return "DEMO_ONLY";
        }
        if (quality == null) {
            return "UNKNOWN";
        }
        if (quality.qualityScore().compareTo(new BigDecimal("0.45")) < 0
            || health != null && health.successRate().compareTo(new BigDecimal("0.60")) < 0) {
            return "LOW";
        }
        if (quality.qualityScore().compareTo(new BigDecimal("0.75")) >= 0) {
            return "HIGH";
        }
        return "MEDIUM";
    }

    /** 生成前端数据源看板提示文案。 */
    private String resolveDisplayMessage(
        DataSource source,
        DataSourceHealthView health,
        DataQualitySnapshotView quality,
        String qualityLevel
    ) {
        return switch (qualityLevel) {
            case "DISABLED" -> "数据源已停用，不参与采集和投资分析。";
            case "DEMO_ONLY" -> "当前为兜底或演示数据源，只能用于链路验证，禁止进入正式投资方案。";
            case "UNKNOWN" -> "缺少质量快照，前端应提示补充数据质量评估。";
            case "LOW" -> health != null && health.failureReason() != null
                ? "数据源质量或成功率不足：" + health.failureReason()
                : "数据源质量不足，报告应降级为低可信或数据缺口。";
            case "HIGH" -> "数据源质量较高，可作为投资报告的重要输入。";
            default -> "数据源质量中等，建议结合其他来源交叉验证。";
        };
    }

    /** 获取必需的数据源。 */
    private DataSource requiredSource(String sourceCode) {
        String safeCode = normalizeCode(sourceCode, "数据源编码不能为空");
        return sources.findBySourceCode(safeCode)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "数据源不存在"));
    }

    /** 转换健康状态视图。 */
    private DataSourceHealthView toHealthView(DataSourceHealth health) {
        return DataSourceHealthView.builder()
            .sourceCode(health.sourceCode())
            .lastSuccessAt(health.lastSuccessAt())
            .lastFailureAt(health.lastFailureAt())
            .successRate(health.successRate())
            .avgLatencyMs(health.avgLatencyMs())
            .failureReason(health.failureReason())
            .sampleCount(health.sampleCount())
            .updatedAt(health.updatedAt())
            .build();
    }

    /** 转换质量快照视图。 */
    private DataQualitySnapshotView toQualityView(DataQualitySnapshot snapshot) {
        return DataQualitySnapshotView.builder()
            .bizId(snapshot.bizId())
            .sourceCode(snapshot.sourceCode())
            .dataType(snapshot.dataType())
            .qualityScore(snapshot.qualityScore())
            .missingRate(snapshot.missingRate())
            .duplicateRate(snapshot.duplicateRate())
            .freshnessScore(snapshot.freshnessScore())
            .sampleCount(snapshot.sampleCount())
            .snapshotTime(snapshot.snapshotTime())
            .detail(snapshot.detail())
            .build();
    }

    /** 规范化编码。 */
    private String normalizeCode(String value, String message) {
        return limitText(normalizeText(value, message).toUpperCase(Locale.ROOT), SOURCE_CODE_MAX_LENGTH);
    }

    /** 规范化并校验枚举字符串。 */
    private String normalizeAllowed(String value, Set<String> allowed, String message) {
        String normalized = normalizeCode(value, message);
        if (!allowed.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    /** 规范化必填文本。 */
    private String normalizeText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    /** 规范化可空文本。 */
    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 限制短字段长度，避免 AI 输出长说明导致数据库截断。 */
    private String limitText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 规范化维护方字段，长说明应进入 description 或 collectionPlan。 */
    private String normalizeOwner(String value) {
        String owner = trimToNull(value);
        if (owner == null) {
            return null;
        }
        String compact = owner.replaceAll("\\s+", " ").trim();
        if (compact.length() <= OWNER_MAX_LENGTH) {
            return compact;
        }
        return compact.substring(0, OWNER_MAX_LENGTH);
    }

    /** 规范化采集频率，避免模型把采集计划长文本误写入短频率字段。 */
    private String normalizeFetchFrequency(String value) {
        String frequency = trimToNull(value);
        if (frequency == null) {
            return null;
        }
        String compact = frequency.replaceAll("\\s+", " ").trim();
        if (compact.length() <= FETCH_FREQUENCY_MAX_LENGTH) {
            return compact;
        }
        String upper = compact.toUpperCase(Locale.ROOT);
        if (upper.contains("REALTIME") || compact.contains("实时")) {
            return "REALTIME";
        }
        if (upper.contains("HOURLY") || compact.contains("每小时") || compact.contains("小时")) {
            return "HOURLY";
        }
        if (upper.contains("DAILY") || compact.contains("每日") || compact.contains("每天")) {
            return "DAILY";
        }
        if (upper.contains("WEEKLY") || compact.contains("每周")) {
            return "WEEKLY";
        }
        return compact.substring(0, FETCH_FREQUENCY_MAX_LENGTH);
    }

    /** 规范化 0 到 1 之间的小数。 */
    private BigDecimal normalizeRatio(BigDecimal value, String fieldName) {
        BigDecimal ratio = value == null ? BigDecimal.ZERO : value;
        if (ratio.compareTo(BigDecimal.ZERO) < 0 || ratio.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "必须在0到1之间");
        }
        return ratio.setScale(4, RoundingMode.HALF_UP);
    }

    /** 校验 JSON 文本。 */
    private void validateJson(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            JSON.parse(value);
        } catch (JSONException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "质量快照详情必须是合法JSON");
        }
    }

    /** 规范化质量快照返回数量。 */
    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        if (limit < 1 || limit > 200) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "质量快照数量必须在1到200之间");
        }
        return limit;
    }

    /** 解析数据源发现候选数量。 */
    private int normalizeDiscoveryLimit(Integer limit, String bindingConfig) {
        if (limit != null && limit > 0) {
            return Math.min(limit, 20);
        }
        JSONObject config = parseObject(bindingConfig);
        int configured = config.getIntValue("candidateLimit", 8);
        return Math.max(1, Math.min(configured, 20));
    }

    /** 解析数据源发现目标数据类型。 */
    private List<String> discoveryDataTypes(String dataTypes) {
        if (dataTypes == null || dataTypes.isBlank()) {
            return List.of("MARKET_QUOTE", "NEWS", "ANNOUNCEMENT", "RESEARCH", "REGULATORY");
        }
        return Arrays.stream(dataTypes.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(value -> normalizeAllowed(value, DATA_TYPES, "数据质量类型不合法"))
            .distinct()
            .toList();
    }

    /** 解析偏好的来源等级。 */
    private List<String> discoveryTrustLevels(String trustLevels) {
        if (trustLevels == null || trustLevels.isBlank()) {
            return List.of();
        }
        return Arrays.stream(trustLevels.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(value -> normalizeAllowed(value, TRUST_LEVELS, "来源等级不合法"))
            .distinct()
            .toList();
    }

    /** 通过大模型整理收集数据源候选，远程模型不可用时直接阻断。 */
    private List<DataSourceDiscoveryCandidateView> discoverByModel(
        DiscoverDataSourcesCommand command,
        AiModel model,
        AiSkill skill,
        List<String> dataTypes,
        List<String> trustLevels,
        int limit,
        String promptPreview
    ) {
        var runtimeConfig = modelRuntimeConfigs.resolve(model);
        if (runtimeConfig.mockEnabled()) {
            log.error(
                "AI数据源发现失败: modelCode={}, modelVersion={}, providerCode={}, skillCode={}, collectionDirection={}, reason=模型配置mockEnabled=true",
                runtimeConfig.modelCode(),
                runtimeConfig.modelVersion(),
                runtimeConfig.providerCode(),
                skill.skillCode(),
                command.collectionDirection()
            );
            throw new BusinessException(HttpStatus.BAD_REQUEST, "数据源发现模型配置mockEnabled=true，不能调用远程模型");
        }
        AiJsonCompletionClient client = aiJsonClients.stream()
            .filter(candidate -> candidate.supports(runtimeConfig.providerCode()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                "未找到数据源发现模型客户端: " + runtimeConfig.providerCode()));
        String content = client.completeJson(
            "DATA_SOURCE_COLLECTION_DISCOVERY",
            discoverySystemPrompt(skill),
            promptPreview,
            runtimeConfig
        );
        if (content == null || content.isBlank()) {
            log.error(
                "AI数据源发现失败: modelCode={}, modelVersion={}, providerCode={}, skillCode={}, dataTypes={}, limit={}, reason=模型返回为空",
                runtimeConfig.modelCode(),
                runtimeConfig.modelVersion(),
                runtimeConfig.providerCode(),
                skill.skillCode(),
                dataTypes,
                limit
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "数据源发现模型返回为空，不能使用本地候选兜底");
        }
        List<DataSourceDiscoveryCandidateView> candidates = parseModelCandidates(content, dataTypes, trustLevels, limit);
        log.info(
            "AI数据源发现模型候选解析完成: modelCode={}, modelVersion={}, providerCode={}, skillCode={}, contentLength={}, parsedCandidateCount={}",
            runtimeConfig.modelCode(),
            runtimeConfig.modelVersion(),
            runtimeConfig.providerCode(),
            skill.skillCode(),
            content.length(),
            candidates.size()
        );
        return candidates;
    }

    /** 解析大模型返回的数据源候选 JSON。 */
    private List<DataSourceDiscoveryCandidateView> parseModelCandidates(
        String content,
        List<String> dataTypes,
        List<String> trustLevels,
        int limit
    ) {
        try {
            JSONObject root = JSON.parseObject(content);
            JSONArray candidateArray = root.getJSONArray("candidates");
            if (candidateArray == null || candidateArray.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "数据源发现模型输出缺少candidates数组");
            }
            return candidateArray.stream()
                .filter(JSONObject.class::isInstance)
                .map(JSONObject.class::cast)
                .map(this::toModelCandidate)
                .filter(candidate -> acceptsSourceType(dataTypes, candidate.sourceType()))
                .filter(candidate -> trustLevels.isEmpty() || trustLevels.contains(candidate.trustLevel()))
                .limit(limit)
                .toList();
        } catch (Exception exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "数据源发现模型输出解析失败: " + exception.getMessage());
        }
    }

    /** 将模型输出对象转换为数据源候选视图。 */
    private DataSourceDiscoveryCandidateView toModelCandidate(JSONObject node) {
        String sourceType = normalizeAllowed(node.getString("sourceType"), SOURCE_TYPES, "模型输出数据源类型不合法");
        String trustLevel = normalizeAllowed(node.getString("trustLevel"), TRUST_LEVELS, "模型输出来源等级不合法");
        return DataSourceDiscoveryCandidateView.builder()
            .sourceCode(normalizeCode(node.getString("sourceCode"), "模型输出数据源编码不能为空"))
            .sourceName(normalizeText(node.getString("sourceName"), "模型输出数据源名称不能为空"))
            .sourceType(sourceType)
            .trustLevel(trustLevel)
            .baseUrl(trimToNull(node.getString("baseUrl")))
            .fetchFrequency(normalizeFetchFrequency(node.getString("fetchFrequency")))
            .owner(trimToNull(node.getString("owner")))
            .description(trimToNull(node.getString("description")))
            .recommendedTaskType(defaultText(node.getString("recommendedTaskType"), "AI_DATA_SOURCE_DISCOVERY"))
            .suggestedParameters(stringMap(node.getJSONObject("suggestedParameters")))
            .fieldMappings(stringMap(node.getJSONObject("fieldMappings")))
            .collectionPlan(trimToNull(node.getString("collectionPlan")))
            .qualityPolicy(trimToNull(node.getString("qualityPolicy")))
            .confidence(normalizeConfidence(node.getBigDecimal("confidence")))
            .reasons(stringList(node.getJSONArray("reasons")))
            .requiresReview(node.getBoolean("requiresReview") == null || Boolean.TRUE.equals(node.getBoolean("requiresReview")))
            .build();
    }

    /** 构建默认数据源候选，仅用于 mock 模式和模型空输出兜底。 */
    private List<DataSourceDiscoveryCandidateView> defaultDiscoveryCandidates(List<String> dataTypes) {
        List<DataSourceDiscoveryCandidateView> candidates = new ArrayList<>();
        if (dataTypes.contains("REGULATORY")) {
            candidates.add(candidate("CSRC", "中国证监会", "REGULATORY", "L1", "https://www.csrc.gov.cn",
                "0 0 */6 * * *", "监管披露与政策公告", "REGULATORY_DISCLOSURE_COLLECTION",
                Map.of("responseFormat", "HTML", "freshnessHours", "168", "maxItems", "80"),
                Map.of("title", "页面标题", "publishTime", "发布时间", "content", "正文"),
                "官方监管来源，适合补充政策、处罚、制度和监管事件。",
                "解析监管公开栏目、政策文件、处罚公告和新闻发布；采集后按发布时间、标题、正文、链接去重。",
                "L1来源、72小时内优先、标题/正文/发布时间必填、重复率低于10%。",
                new BigDecimal("0.92")));
        }
        if (dataTypes.contains("ANNOUNCEMENT")) {
            candidates.add(candidate("CNINFO", "巨潮资讯", "ANNOUNCEMENT", "L1", "https://www.cninfo.com.cn",
                "0 20 */4 * * *", "公告与信息披露", "EXCHANGE_ANNOUNCEMENT_COLLECTION",
                Map.of("responseFormat", "JSON", "freshnessHours", "168", "maxItems", "100"),
                Map.of("externalId", "announcementId", "title", "announcementTitle",
                    "publishTime", "announcementTime", "url", "adjunctUrl"),
                "上市公司公告和披露文档集中来源，适合和交易所公告交叉验证。",
                "围绕公告检索接口、PDF附件和公司代码整理采集方案；保留公告ID和附件URL。",
                "L1来源、公告ID唯一、发布时间必填、附件链接可追溯。",
                new BigDecimal("0.90")));
            candidates.add(candidate("SSE", "上海证券交易所", "ANNOUNCEMENT", "L1", "https://www.sse.com.cn",
                "0 10 */4 * * *", "上交所公告", "EXCHANGE_ANNOUNCEMENT_COLLECTION",
                Map.of("responseFormat", "JSON", "freshnessHours", "168", "maxItems", "100"),
                Map.of("title", "title", "publishTime", "publishDate", "url", "url"),
                "交易所官方披露来源，适合校验上市公司和 ETF 公告。",
                "按市场栏目整理公告分页、ETF公告和监管问询来源；和巨潮交叉去重。",
                "L1来源、交易所域名、公告标题和日期必填。",
                new BigDecimal("0.88")));
        }
        if (dataTypes.contains("MARKET_QUOTE")) {
            candidates.add(candidate("CHINA_WEALTH", "中国理财网", "MARKET", "L2", "https://www.chinawealth.com.cn",
                "0 30 */6 * * *", "银行理财产品与净值", "WEALTH_PRODUCT_NAV_REFRESH",
                Map.of("responseFormat", "HTML", "productMarketCode", "BANK_WMP", "quoteInterval", "1D"),
                Map.of("productCode", "产品登记编码", "productName", "产品名称",
                    "nav", "单位净值", "riskLevel", "风险等级"),
                "银行理财产品官方披露入口，适合补产品池和净值行情。",
                "整理产品登记、发行机构、风险等级、净值披露和产品状态字段；用于产品池upsert和净值行情。",
                "L2来源、产品编码唯一、净值为数字、风险等级可映射。",
                new BigDecimal("0.86")));
        }
        if (dataTypes.contains("NEWS")) {
            candidates.add(candidate("EASTMONEY", "东方财富", "NEWS", "L4", "https://www.eastmoney.com",
                "0 */30 * * * *", "财经新闻和市场热度", "AI_DATA_SOURCE_DISCOVERY",
                Map.of("sourceReviewRequired", "true", "fallbackEnabled", "false", "freshnessHours", "72"),
                Map.of("title", "由Skill生成", "summary", "由Skill生成", "publishTime", "由Skill生成", "url", "由Skill生成"),
                "主流财经媒体只作为候选线索，由数据源发现Skill继续校验，不再默认推荐RSS采集。",
                "整理新闻栏目、主题关键词、发布时间和原文链接；仅作为热度补充，不单独形成投资建议。",
                "L4来源、需至少一个L1/L2来源交叉验证、禁止fallback。",
                new BigDecimal("0.72")));
        }
        if (dataTypes.contains("RESEARCH")) {
            candidates.add(candidate("CHOICE", "Choice 数据", "RESEARCH", "L3", "https://choice.eastmoney.com",
                "0 0 */12 * * *", "研报与专业数据供应商", "VENDOR_MARKET_QUOTE_SYNC",
                Map.of("responseFormat", "VENDOR_API", "requiresLicense", "true", "freshnessHours", "24"),
                Map.of("reportTitle", "title", "publishTime", "publishTime", "institution", "institution"),
                "专业供应商候选，需要授权后启用，适合作为研报和行情增强源。",
                "整理供应商API、授权条件、研报字段、行情字段和费用风险；未授权时只进入候选池。",
                "L3来源、必须标注授权要求和供应商限制。",
                new BigDecimal("0.80")));
        }
        return candidates;
    }

    /** 构建单个数据源发现候选。 */
    private DataSourceDiscoveryCandidateView candidate(
        String sourceCode,
        String sourceName,
        String sourceType,
        String trustLevel,
        String baseUrl,
        String fetchFrequency,
        String owner,
        String taskType,
        Map<String, String> parameters,
        Map<String, String> fieldMappings,
        String reason,
        String collectionPlan,
        String qualityPolicy,
        BigDecimal confidence
    ) {
        return DataSourceDiscoveryCandidateView.builder()
            .sourceCode(sourceCode)
            .sourceName(sourceName)
            .sourceType(sourceType)
            .trustLevel(trustLevel)
            .baseUrl(baseUrl)
            .fetchFrequency(fetchFrequency)
            .owner(owner)
            .description(reason)
            .recommendedTaskType(taskType)
            .suggestedParameters(new LinkedHashMap<>(parameters))
            .fieldMappings(new LinkedHashMap<>(fieldMappings))
            .collectionPlan(collectionPlan)
            .qualityPolicy(qualityPolicy)
            .confidence(confidence)
            .reasons(List.of(reason))
            .requiresReview(true)
            .build();
    }

    /** 构造前端可展示和可复盘的 Prompt 预览。 */
    private String discoveryPromptPreview(
        DiscoverDataSourcesCommand command,
        AiModelBinding binding,
        AiSkill skill,
        List<String> dataTypes,
        List<String> trustLevels,
        int limit
    ) {
        return """
            skillCode=%s
            skillVersion=%s
            skillInstruction=已通过system prompt提供，本轮user prompt不重复展开。
            你需要像数据采集专家一样，直接整理、收集并生成可执行的数据源方案。
            不要只给泛泛建议；必须输出可落库的数据源候选、采集计划、字段映射、质量规则和限制。
            输出JSON对象，顶层字段必须包含 candidates 数组。每个候选必须包含：
            sourceCode、sourceName、sourceType、trustLevel、baseUrl、fetchFrequency、owner、
            recommendedTaskType、suggestedParameters、fieldMappings、collectionPlan、qualityPolicy、
            confidence、reasons、requiresReview。
            sourceType只能是 MARKET、NEWS、ANNOUNCEMENT、RESEARCH、REGULATORY。
            trustLevel只能是 L1、L2、L3、L4、L5。
            sourceCode最长64字符，sourceName最长128字符，baseUrl最长512字符，owner最长255字符。
            fetchFrequency只能填写 cron 表达式或短频率枚举/短语，例如 REALTIME、HOURLY、DAILY、WEEKLY、
            0 0 */6 * * *；不得填写采集计划、限制说明或长段落。
            candidates数量不得超过candidateLimit。
            fieldMappings每个候选最多5项，reasons最多2条。
            description、collectionPlan、qualityPolicy均控制在80个汉字以内。
            禁止把低质量媒体源或兜底数据标记为正式投资依据。
            marketScope=%s
            assetClass=%s
            dataTypes=%s
            collectionDirection=%s
            topicKeywords=%s
            preferredTrustLevels=%s
            candidateLimit=%s
            modelCode=%s
            """.formatted(
            skill == null ? DEFAULT_DISCOVERY_SKILL : skill.skillCode(),
            skill == null ? "" : skill.skillVersion(),
            defaultText(command.marketScope(), CN_MAINLAND),
            defaultText(command.assetClass(), "MULTI_ASSET"),
            dataTypes,
            defaultText(command.collectionDirection(), "MULTI_SOURCE"),
            defaultText(command.topicKeywords(), ""),
            trustLevels,
            limit,
            binding.modelCode()
        );
    }

    /** 构建模型系统提示词。 */
    private String discoverySystemPrompt(AiSkill skill) {
        return """
            你是投资理财平台的数据采集与治理专家。你的任务是根据Skill指令和用户输入，
            整理可执行的数据源、采集计划、字段映射和质量规则。必须输出JSON对象，不得输出Markdown。
            Skill指令：
            %s
            """.formatted(skill.instructionContent());
    }

    /** 根据请求选择启用 Skill。 */
    private AiSkill resolveDiscoverySkill(DiscoverDataSourcesCommand command) {
        String skillCode = trimToNull(command.skillCode());
        if (skillCode == null) {
            skillCode = switch (defaultText(command.collectionDirection(), "").toUpperCase(Locale.ROOT)) {
                case "OFFICIAL_DISCLOSURE" -> "DATA_COLLECTION_OFFICIAL_DISCLOSURE";
                case "NEWS_RESEARCH" -> "DATA_COLLECTION_NEWS_RESEARCH";
                case "PRODUCT_NAV" -> "DATA_COLLECTION_PRODUCT_NAV";
                case "MARKET_QUOTE" -> "DATA_COLLECTION_MARKET_QUOTE";
                case "REGULATORY" -> "DATA_COLLECTION_REGULATORY";
                default -> DEFAULT_DISCOVERY_SKILL;
            };
        }
        String safeSkillCode = skillCode.trim().toUpperCase(Locale.ROOT);
        return skills.findActiveByCode(safeSkillCode)
            .orElseGet(() -> skills.findActiveByCode(DEFAULT_DISCOVERY_SKILL)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                    "启用的数据采集Skill不存在: " + safeSkillCode)));
    }

    /** 判断模型输出的数据源类型是否覆盖本次请求的数据类型。 */
    private boolean acceptsSourceType(List<String> dataTypes, String sourceType) {
        if (dataTypes.isEmpty()) {
            return true;
        }
        return dataTypes.stream()
            .map(DATA_TYPE_SOURCE_TYPES::get)
            .anyMatch(expectedSourceType -> sourceType.equals(expectedSourceType));
    }

    /** 规范化模型候选置信度，避免模型输出越界值污染前端评分。 */
    private BigDecimal normalizeConfidence(BigDecimal confidence) {
        BigDecimal value = confidence == null ? new BigDecimal("0.70") : confidence;
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            value = BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            value = BigDecimal.ONE;
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    /** JSON 对象转字符串 Map。 */
    private Map<String, String> stringMap(JSONObject object) {
        if (object == null || object.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        object.forEach((key, value) -> result.put(key, jsonValueText(value)));
        return result;
    }

    /** JSON 数组转字符串列表。 */
    private List<String> stringList(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        return array.stream().map(String::valueOf).toList();
    }

    /** 将模型输出的参数值转换为稳定字符串，嵌套对象保留 JSON 文本。 */
    private String jsonValueText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return JSON.toJSONString(value);
    }

    /** 解析 JSON 对象配置。 */
    private JSONObject parseObject(String value) {
        if (value == null || value.isBlank()) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(value);
        } catch (JSONException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "数据源发现模型配置JSON不合法");
        }
    }

    /** 空文本默认值。 */
    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
