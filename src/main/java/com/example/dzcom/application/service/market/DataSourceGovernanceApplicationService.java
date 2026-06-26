package com.example.dzcom.application.service.market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
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
import com.example.dzcom.application.service.ai.AiModelBindingApplicationService;
import com.example.dzcom.domain.model.ai.AiModelBinding;
import com.example.dzcom.domain.model.ai.AiSkill;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.repository.ai.AiSkillStore;
import com.example.dzcom.domain.repository.market.DataSourceSearchCriteria;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import lombok.RequiredArgsConstructor;
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
public class DataSourceGovernanceApplicationService {
    private static final Set<String> SOURCE_TYPES =
        Set.of("MARKET", "NEWS", "ANNOUNCEMENT", "RESEARCH", "REGULATORY", "FALLBACK");
    private static final Set<String> TRUST_LEVELS = Set.of("L1", "L2", "L3", "L4", "L5");
    private static final Set<String> DATA_TYPES =
        Set.of("MARKET_QUOTE", "NEWS", "ANNOUNCEMENT", "RESEARCH", "REGULATORY");
    private static final Set<String> SORT_FIELDS =
        Set.of("updatedAt", "sourceCode", "sourceName", "sourceType", "trustLevel", "enabled");
    private static final String CN_MAINLAND = "CN_MAINLAND";
    private static final String DATA_SOURCE_DISCOVERY_SKILL = "DATA_SOURCE_DISCOVERY_CORE";

    private final DataSourceStore sources;
    private final AiModelBindingApplicationService modelBindings;
    private final AiSkillStore skills;
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
        String sourceCode = normalizeCode(command.sourceCode(), "数据源编码不能为空");
        String sourceType = normalizeAllowed(command.sourceType(), SOURCE_TYPES, "数据源类型不合法");
        String trustLevel = normalizeAllowed(command.trustLevel(), TRUST_LEVELS, "来源等级不合法");
        DataSource existing = sources.findBySourceCode(sourceCode).orElse(null);
        LocalDateTime now = clock.now();
        DataSource source = DataSource.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .sourceName(normalizeText(command.sourceName(), "数据源名称不能为空"))
            .sourceType(sourceType)
            .trustLevel(trustLevel)
            .baseUrl(trimToNull(command.baseUrl()))
            .enabled(command.enabled() == null || command.enabled())
            .fetchFrequency(trimToNull(command.fetchFrequency()))
            .owner(trimToNull(command.owner()))
            .description(trimToNull(command.description()))
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
        AiModelBinding binding = modelBindings.enabledBinding(
            AiModelBindingApplicationService.DATA_SOURCE_DISCOVERY,
            command.environment()
        );
        AiSkill skill = skills.findActiveByCode(DATA_SOURCE_DISCOVERY_SKILL).orElse(null);
        int limit = normalizeDiscoveryLimit(command.candidateLimit(), binding.config());
        List<String> dataTypes = discoveryDataTypes(command.dataTypes());
        List<String> trustLevels = discoveryTrustLevels(command.preferredTrustLevels());
        List<DataSourceDiscoveryCandidateView> candidates = defaultDiscoveryCandidates(dataTypes).stream()
            .filter(candidate -> trustLevels.isEmpty() || trustLevels.contains(candidate.trustLevel()))
            .filter(candidate -> Boolean.TRUE.equals(command.includeDisabledCandidates())
                || !"L3".equals(candidate.trustLevel()))
            .limit(limit)
            .toList();
        return DataSourceDiscoveryView.builder()
            .scenarioCode(binding.scenarioCode())
            .modelCode(binding.modelCode())
            .providerCode(binding.providerCode())
            .environment(binding.environment())
            .marketScope(defaultText(command.marketScope(), CN_MAINLAND))
            .assetClass(defaultText(command.assetClass(), "MULTI_ASSET"))
            .dataTypes(String.join(",", dataTypes))
            .topicKeywords(defaultText(command.topicKeywords(), ""))
            .modelBindingConfig(new LinkedHashMap<>(parseObject(binding.config())))
            .skillCode(skill == null ? DATA_SOURCE_DISCOVERY_SKILL : skill.skillCode())
            .skillVersion(skill == null ? "" : skill.skillVersion())
            .skillInstruction(skill == null ? "未找到启用的 Skill，将使用系统内置发现约束。" : skill.instructionContent())
            .candidates(candidates)
            .reviewPolicy("AI 只生成候选；正式保存、启用采集和字段映射必须由前端人工确认。")
            .promptPreview(discoveryPromptPreview(command, binding, skill, dataTypes, trustLevels, limit))
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
        return normalizeText(value, message).toUpperCase(Locale.ROOT);
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

    /** 构建默认数据源候选，后续可替换为真实大模型联网检索结果。 */
    private List<DataSourceDiscoveryCandidateView> defaultDiscoveryCandidates(List<String> dataTypes) {
        List<DataSourceDiscoveryCandidateView> candidates = new ArrayList<>();
        if (dataTypes.contains("REGULATORY")) {
            candidates.add(candidate("CSRC", "中国证监会", "REGULATORY", "L1", "https://www.csrc.gov.cn",
                "0 0 */6 * * *", "监管披露与政策公告", "REGULATORY_DISCLOSURE_COLLECTION",
                Map.of("responseFormat", "HTML", "freshnessHours", "168", "maxItems", "80"),
                Map.of("title", "页面标题", "publishTime", "发布时间", "content", "正文"),
                "官方监管来源，适合补充政策、处罚、制度和监管事件。", new BigDecimal("0.92")));
        }
        if (dataTypes.contains("ANNOUNCEMENT")) {
            candidates.add(candidate("CNINFO", "巨潮资讯", "ANNOUNCEMENT", "L1", "https://www.cninfo.com.cn",
                "0 20 */4 * * *", "公告与信息披露", "EXCHANGE_ANNOUNCEMENT_COLLECTION",
                Map.of("responseFormat", "JSON", "freshnessHours", "168", "maxItems", "100"),
                Map.of("externalId", "announcementId", "title", "announcementTitle",
                    "publishTime", "announcementTime", "url", "adjunctUrl"),
                "上市公司公告和披露文档集中来源，适合和交易所公告交叉验证。", new BigDecimal("0.90")));
            candidates.add(candidate("SSE", "上海证券交易所", "ANNOUNCEMENT", "L1", "https://www.sse.com.cn",
                "0 10 */4 * * *", "上交所公告", "EXCHANGE_ANNOUNCEMENT_COLLECTION",
                Map.of("responseFormat", "JSON", "freshnessHours", "168", "maxItems", "100"),
                Map.of("title", "title", "publishTime", "publishDate", "url", "url"),
                "交易所官方披露来源，适合校验上市公司和 ETF 公告。", new BigDecimal("0.88")));
        }
        if (dataTypes.contains("MARKET_QUOTE")) {
            candidates.add(candidate("CHINA_WEALTH", "中国理财网", "MARKET", "L2", "https://www.chinawealth.com.cn",
                "0 30 */6 * * *", "银行理财产品与净值", "WEALTH_PRODUCT_NAV_REFRESH",
                Map.of("responseFormat", "HTML", "productMarketCode", "BANK_WMP", "quoteInterval", "1D"),
                Map.of("productCode", "产品登记编码", "productName", "产品名称",
                    "nav", "单位净值", "riskLevel", "风险等级"),
                "银行理财产品官方披露入口，适合补产品池和净值行情。", new BigDecimal("0.86")));
        }
        if (dataTypes.contains("NEWS")) {
            candidates.add(candidate("EASTMONEY", "东方财富", "NEWS", "L4", "https://www.eastmoney.com",
                "0 */30 * * * *", "财经新闻和市场热度", "AI_DATA_SOURCE_DISCOVERY",
                Map.of("sourceReviewRequired", "true", "fallbackEnabled", "false", "freshnessHours", "72"),
                Map.of("title", "由Skill生成", "summary", "由Skill生成", "publishTime", "由Skill生成", "url", "由Skill生成"),
                "主流财经媒体只作为候选线索，由数据源发现Skill继续校验，不再默认推荐RSS采集。", new BigDecimal("0.72")));
        }
        if (dataTypes.contains("RESEARCH")) {
            candidates.add(candidate("CHOICE", "Choice 数据", "RESEARCH", "L3", "https://choice.eastmoney.com",
                "0 0 */12 * * *", "研报与专业数据供应商", "VENDOR_MARKET_QUOTE_SYNC",
                Map.of("responseFormat", "VENDOR_API", "requiresLicense", "true", "freshnessHours", "24"),
                Map.of("reportTitle", "title", "publishTime", "publishTime", "institution", "institution"),
                "专业供应商候选，需要授权后启用，适合作为研报和行情增强源。", new BigDecimal("0.80")));
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
            skillInstruction=%s
            请为投资平台推荐高质量数据源候选。要求优先官方、监管、交易所和产品披露来源；
            不允许把低质量媒体源标记为正式投资依据；输出必须包含 sourceCode、sourceName、
            sourceType、trustLevel、baseUrl、recommendedTaskType、fieldMappings、confidence、requiresReview。
            marketScope=%s
            assetClass=%s
            dataTypes=%s
            preferredTrustLevels=%s
            candidateLimit=%s
            modelCode=%s
            """.formatted(
            skill == null ? DATA_SOURCE_DISCOVERY_SKILL : skill.skillCode(),
            skill == null ? "" : skill.skillVersion(),
            skill == null ? "SYSTEM_BUILT_IN_DISCOVERY_POLICY" : skill.instructionContent(),
            defaultText(command.marketScope(), CN_MAINLAND),
            defaultText(command.assetClass(), "MULTI_ASSET"),
            dataTypes,
            trustLevels,
            limit,
            binding.modelCode()
        );
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
