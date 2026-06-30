package com.example.dzcom.application.service.system;

import com.example.dzcom.application.command.system.SaveSystemConfigCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.system.SystemConfigView;
import com.example.dzcom.domain.model.system.SystemConfig;
import com.example.dzcom.domain.repository.system.SystemConfigStore;
import com.example.dzcom.domain.repository.system.SystemConfigSearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** 数据库系统配置读取服务。 */
@Service
@RequiredArgsConstructor
public class SystemConfigApplicationService implements SystemConfigReader {
    private static final String DEFAULT_ENVIRONMENT = "DEFAULT";
    private static final String AUTO_CLOSED_LOOP_PROFILE_GROUP = "AUTO_INVESTMENT_CLOSED_LOOP_PROFILE";
    private static final String DEFAULT_AUTO_CLOSED_LOOP_PROFILE_KEY = "default-auto-mock";
    private static final Set<String> VALUE_TYPES = Set.of("STRING", "NUMBER", "BOOLEAN", "JSON");
    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");
    private static final Set<String> SORTS = Set.of(
        "updatedAt", "configGroup", "configKey", "environment", "valueType", "status");

    private final SystemConfigStore configs;
    private final Environment environment;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 读取字符串配置。 */
    @Override
    public Optional<String> stringValue(String configGroup, String configKey) {
        return findEnabled(configGroup, configKey)
            .map(SystemConfig::configValue)
            .map(Jsons::readTree)
            .map(Jsons::valueText)
            .filter(value -> !value.isBlank());
    }

    /** 读取数值配置。 */
    @Override
    public Optional<BigDecimal> decimalValue(String configGroup, String configKey) {
        return findEnabled(configGroup, configKey)
            .map(SystemConfig::configValue)
            .map(Jsons::readTree)
            .map(this::decimalValue);
    }

    /** 分页查询系统配置。 */
    @Transactional(readOnly = true)
    public PageResult<SystemConfigView> list(
        String configGroup,
        String keyword,
        String configEnvironment,
        String status,
        PageQuery query
    ) {
        PageResult<SystemConfig> page = configs.search(new SystemConfigSearchCriteria(
            trimToNull(configGroup) == null ? null : normalizeCode(configGroup, "配置分组不能为空"),
            trimToNull(keyword),
            trimToNull(configEnvironment) == null ? null : normalizeEnvironment(configEnvironment),
            trimToNull(status) == null ? null : normalizeStatus(status),
            query.page(),
            query.size(),
            query.safeSort(SORTS, "updatedAt"),
            "asc".equalsIgnoreCase(query.direction())
        ));
        List<SystemConfig> items = withAutoClosedLoopProfileFallback(
            trimToNull(configGroup) == null ? null : normalizeCode(configGroup, "配置分组不能为空"),
            trimToNull(configEnvironment) == null ? null : normalizeEnvironment(configEnvironment),
            trimToNull(status) == null ? null : normalizeStatus(status),
            page.items());
        long total = page.total() == 0 && !items.isEmpty() ? items.size() : page.total();
        int totalPages = page.totalPages() == 0 && total > 0 ? 1 : page.totalPages();
        return PageResult.<SystemConfigView>builder()
            .items(items.stream().map(this::toView).toList())
            .total(total)
            .page(page.page())
            .size(page.size())
            .totalPages(totalPages)
            .build();
    }

    /**
     * 方案种子未落库时返回默认闭环方案，保证前端选择器仍能按权威默认方案继续运行。
     */
    private List<SystemConfig> withAutoClosedLoopProfileFallback(
        String configGroup,
        String configEnvironment,
        String status,
        List<SystemConfig> items
    ) {
        if (!AUTO_CLOSED_LOOP_PROFILE_GROUP.equals(configGroup)
            || (configEnvironment != null && !DEFAULT_ENVIRONMENT.equals(configEnvironment))
            || (status != null && !"ENABLED".equals(status))
            || !items.isEmpty()) {
            return items;
        }
        return new ArrayList<>(List.of(defaultAutoClosedLoopProfile()));
    }

    /** 构造自动闭环默认方案的只读兜底视图，数据库配置存在时不会使用。 */
    private SystemConfig defaultAutoClosedLoopProfile() {
        LocalDateTime now = clock.now();
        return SystemConfig.builder()
            .bizId("41000000-0000-0000-0000-000000000010")
            .configGroup(AUTO_CLOSED_LOOP_PROFILE_GROUP)
            .configKey(DEFAULT_AUTO_CLOSED_LOOP_PROFILE_KEY)
            .environment(DEFAULT_ENVIRONMENT)
            .valueType("JSON")
            .configValue("""
                {"profileCode":"default-auto-mock","profileName":"默认 AI Mock 闭环方案","profileType":"SCHEDULED_BASELINE","riskLevel":"LOW","strategyNote":"默认定时闭环方案：采集、报告、候选、Mock交易、回测反馈全链路执行；真实交易保持关闭。","automationLevel":"FULL_MOCK","mockPortfolioBizId":"","mockUserBizId":"21000000-0000-0000-0000-000000000002","mockPortfolioName":"全自动闭环模拟组合","initialCash":"100000","promptCode":"investment-plan-from-report","promptVersion":"auto-v1","promptScenario":"INVESTMENT_PLAN","modelType":"INVESTMENT_ANALYSIS","execution":{"runMode":"FULL_PIPELINE","marketScope":"CN_MAINLAND","dataTaskCodes":["real-data-quality-snapshot"],"reportTaskCode":"auto-openai-investment-report-generation","promptTaskCode":"","skipReportTask":false,"allowPromptCandidate":true,"allowModelCandidate":true},"qualityGate":{"requireStructuredCoreData":false,"minQualityScore":"0.45","maxReportsForMock":"20"},"safety":{"allowAutoMockTrade":true,"allowAutoPromptActivation":true,"allowAutoModelActivation":true,"allowRealTrade":false,"maxSingleTradeAmount":"10000"},"backtest":{"benchmarkCode":"","valuationPointLimit":"100"}}
                """.trim())
            .description("自动投资闭环默认配置方案兜底；请优先通过数据库配置表维护正式方案。")
            .status("ENABLED")
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /** 保存系统配置。 */
    @Transactional
    public SystemConfigView save(SaveSystemConfigCommand command) {
        String configGroup = normalizeCode(command.configGroup(), "配置分组不能为空");
        String configKey = normalizeKey(command.configKey());
        String configEnvironment = normalizeEnvironment(command.environment());
        String valueType = normalizeValueType(command.valueType());
        String status = normalizeStatus(command.status());
        String configValue = normalizeConfigValue(valueType, command.configValue());
        SystemConfig existing = configs.findByKey(configGroup, configKey, configEnvironment).orElse(null);
        LocalDateTime now = clock.now();
        SystemConfig saved = configs.save(SystemConfig.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .configGroup(configGroup)
            .configKey(configKey)
            .environment(configEnvironment)
            .valueType(valueType)
            .configValue(configValue)
            .description(trimToNull(command.description()))
            .status(status)
            .version(existing == null ? 0 : existing.version() + 1)
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .build());
        return toView(saved);
    }

    private Optional<SystemConfig> findEnabled(String configGroup, String configKey) {
        for (String currentEnvironment : candidateEnvironments()) {
            Optional<SystemConfig> matched = configs.findEnabled(configGroup, configKey, currentEnvironment);
            if (matched.isPresent()) {
                return matched;
            }
        }
        return Optional.empty();
    }

    private Set<String> candidateEnvironments() {
        Set<String> result = new LinkedHashSet<>();
        Arrays.stream(environment.getActiveProfiles())
            .filter(profile -> profile != null && !profile.isBlank())
            .map(profile -> profile.trim().toUpperCase())
            .forEach(result::add);
        result.add(DEFAULT_ENVIRONMENT);
        return result;
    }

    private BigDecimal decimalValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
    }

    private SystemConfigView toView(SystemConfig config) {
        return SystemConfigView.builder()
            .bizId(config.bizId())
            .configGroup(config.configGroup())
            .configKey(config.configKey())
            .environment(config.environment())
            .valueType(config.valueType())
            .configValue(config.configValue())
            .displayValue(displayValue(config.configValue()))
            .description(config.description())
            .status(config.status())
            .version(config.version())
            .createdAt(config.createdAt())
            .updatedAt(config.updatedAt())
            .build();
    }

    private String normalizeConfigValue(String valueType, Object value) {
        return switch (valueType) {
            case "STRING" -> Jsons.toJson(value == null ? "" : String.valueOf(value));
            case "NUMBER" -> Jsons.toJson(numberValue(value));
            case "BOOLEAN" -> Jsons.toJson(booleanValue(value));
            case "JSON" -> normalizeJsonValue(value);
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的配置值类型");
        };
    }

    private BigDecimal numberValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "数值配置不能为空");
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "数值配置格式不合法");
        }
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        String text = value == null ? "" : String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "布尔配置必须为 true 或 false");
    }

    private String normalizeJsonValue(Object value) {
        if (value == null) {
            return "{}";
        }
        if (value instanceof String textValue) {
            if (textValue.isBlank()) {
                return "{}";
            }
            try {
                return Jsons.readTree(textValue).toString();
            } catch (IllegalArgumentException exception) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "JSON配置格式不合法");
            }
        }
        return Jsons.toJson(value);
    }

    private String displayValue(String configValue) {
        if (configValue == null || configValue.isBlank()) {
            return "";
        }
        return Jsons.valueText(Jsons.readTree(configValue));
    }

    private String normalizeCode(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "配置键不能为空");
        }
        return value.trim();
    }

    private String normalizeEnvironment(String value) {
        return value == null || value.isBlank() ? DEFAULT_ENVIRONMENT : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeValueType(String value) {
        String normalized = normalizeCode(value, "配置值类型不能为空");
        if (!VALUE_TYPES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "配置值类型不支持: " + normalized);
        }
        return normalized;
    }

    private String normalizeStatus(String value) {
        String normalized = value == null || value.isBlank() ? "ENABLED" : value.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "配置状态不支持: " + normalized);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
