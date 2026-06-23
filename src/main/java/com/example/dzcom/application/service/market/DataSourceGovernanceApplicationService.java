package com.example.dzcom.application.service.market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.example.dzcom.application.command.market.SaveDataQualitySnapshotCommand;
import com.example.dzcom.application.command.market.SaveDataSourceCommand;
import com.example.dzcom.application.command.market.SaveDataSourceHealthCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.market.DataQualitySnapshotView;
import com.example.dzcom.application.dto.market.DataSourceHealthView;
import com.example.dzcom.application.dto.market.DataSourceView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.repository.market.DataSourceSearchCriteria;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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

    private final DataSourceStore sources;
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
}
