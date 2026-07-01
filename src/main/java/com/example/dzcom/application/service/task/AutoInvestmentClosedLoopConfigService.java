package com.example.dzcom.application.service.task;

import com.example.dzcom.application.service.system.SystemConfigReader;
import com.example.dzcom.application.common.json.Jsons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 自动投资闭环配置解析服务。 */
@Service
@RequiredArgsConstructor
public class AutoInvestmentClosedLoopConfigService {
    private static final String CONFIG_GROUP = "AUTO_INVESTMENT_CLOSED_LOOP";
    private static final String PROFILE_CONFIG_GROUP = "AUTO_INVESTMENT_CLOSED_LOOP_PROFILE";
    private static final String DEFAULT_AUTOMATION_LEVEL = "FULL_MOCK";
    private static final String DEFAULT_MOCK_USER_BIZ_ID = "21000000-0000-0000-0000-000000000002";
    private static final String DEFAULT_MOCK_PORTFOLIO_NAME = "全自动闭环模拟组合";
    private static final BigDecimal DEFAULT_INITIAL_CASH = new BigDecimal("100000");
    private static final String DEFAULT_PROMPT_CODE = "investment-plan-from-report";
    private static final String DEFAULT_PROMPT_VERSION = "auto-v1";
    private static final String DEFAULT_PROMPT_SCENARIO = "INVESTMENT_PLAN";
    private static final String DEFAULT_MODEL_TYPE = "INVESTMENT_ANALYSIS";
    private static final String DEFAULT_SCHEDULED_PROFILE_CODE = "default-auto-mock";

    private final SystemConfigReader configs;

    /**
     * 读取自动闭环默认自动化等级。
     *
     * @return 自动化等级编码
     * @author dz
     * @date 2026-06-30
     */
    public String automationLevel() {
        return stringValue("automationLevel", DEFAULT_AUTOMATION_LEVEL);
    }

    /**
     * 读取自动闭环默认模拟交易用户。
     *
     * @return 模拟交易用户业务 ID
     * @author dz
     * @date 2026-06-30
     */
    public String mockUserBizId() {
        return stringValue("mockUserBizId", DEFAULT_MOCK_USER_BIZ_ID);
    }

    /**
     * 读取自动闭环默认 Mock 组合。
     *
     * @return Mock 组合业务 ID，未配置时返回空字符串
     * @author dz
     * @date 2026-06-30
     */
    public String mockPortfolioBizId() {
        return stringValue("mockPortfolioBizId", "");
    }

    /**
     * 读取自动闭环默认 Mock 组合名称。
     *
     * @return Mock 组合名称
     * @author dz
     * @date 2026-06-30
     */
    public String mockPortfolioName() {
        return stringValue("mockPortfolioName", DEFAULT_MOCK_PORTFOLIO_NAME);
    }

    /**
     * 读取自动闭环默认初始现金。
     *
     * @return 初始现金金额
     * @author dz
     * @date 2026-06-30
     */
    public BigDecimal initialCash() {
        return configs.decimalValue(CONFIG_GROUP, "initialCash")
            .filter(value -> value.compareTo(BigDecimal.ZERO) > 0)
            .orElse(DEFAULT_INITIAL_CASH);
    }

    /**
     * 读取自动闭环默认 Prompt 编码。
     *
     * @return Prompt 编码
     * @author dz
     * @date 2026-06-30
     */
    public String promptCode() {
        return stringValue("promptCode", DEFAULT_PROMPT_CODE);
    }

    /**
     * 读取自动闭环默认 Prompt 版本。
     *
     * @return Prompt 版本
     * @author dz
     * @date 2026-06-30
     */
    public String promptVersion() {
        return stringValue("promptVersion", DEFAULT_PROMPT_VERSION);
    }

    /**
     * 读取自动闭环默认 Prompt 场景。
     *
     * @return Prompt 场景
     * @author dz
     * @date 2026-06-30
     */
    public String promptScenario() {
        return stringValue("promptScenario", DEFAULT_PROMPT_SCENARIO);
    }

    /**
     * 读取自动闭环默认模型类型。
     *
     * @return 模型类型
     * @author dz
     * @date 2026-06-30
     */
    public String modelType() {
        return stringValue("modelType", DEFAULT_MODEL_TYPE);
    }

    /**
     * 读取定时任务权威配置方案编码。
     *
     * @return 定时触发使用的配置方案编码
     * @author dz
     * @date 2026-06-30
     */
    public String scheduledConfigProfileCode() {
        return stringValue("scheduledConfigProfileCode", DEFAULT_SCHEDULED_PROFILE_CODE);
    }

    /**
     * 读取并展开自动闭环配置方案参数。
     *
     * @param profileCode 配置方案编码
     * @return 展开后的任务参数；方案不存在时为空
     * @author dz
     * @date 2026-06-30
     */
    public Optional<Map<String, String>> profileParameters(String profileCode) {
        if (profileCode == null || profileCode.isBlank()) {
            return Optional.empty();
        }
        String normalizedProfileCode = profileCode.trim();
        return configs.stringValue(PROFILE_CONFIG_GROUP, profileCode.trim())
            .map(Jsons::readObjectMapOrEmpty)
            .map(this::flattenProfileParameters)
            .or(() -> DEFAULT_SCHEDULED_PROFILE_CODE.equals(normalizedProfileCode)
                ? Optional.of(defaultProfileParameters())
                : Optional.empty());
    }

    /**
     * 数据库方案种子缺失时的运行兜底，保证默认定时方案仍能形成完整闭环参数。
     *
     * @return 默认自动闭环方案参数
     * @author dz
     * @date 2026-06-30
     */
    private Map<String, String> defaultProfileParameters() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("profileCode", DEFAULT_SCHEDULED_PROFILE_CODE);
        result.put("profileName", "默认 AI Mock 闭环方案");
        result.put("profileType", "SCHEDULED_BASELINE");
        result.put("riskLevel", "LOW");
        result.put("strategyNote", "默认定时闭环方案：采集、报告、候选、Mock交易、回测反馈全链路执行；真实交易保持关闭。");
        result.put("automationLevel", DEFAULT_AUTOMATION_LEVEL);
        result.put("mockPortfolioBizId", "");
        result.put("mockUserBizId", DEFAULT_MOCK_USER_BIZ_ID);
        result.put("mockPortfolioName", DEFAULT_MOCK_PORTFOLIO_NAME);
        result.put("initialCash", DEFAULT_INITIAL_CASH.toPlainString());
        result.put("promptCode", DEFAULT_PROMPT_CODE);
        result.put("promptVersion", DEFAULT_PROMPT_VERSION);
        result.put("promptScenario", DEFAULT_PROMPT_SCENARIO);
        result.put("modelType", DEFAULT_MODEL_TYPE);
        result.put("runMode", "FULL_PIPELINE");
        result.put("marketScope", "CN_MAINLAND");
        result.put("dataTaskCodes", "real-data-quality-snapshot");
        result.put("reportTaskCode", "auto-openai-investment-report-generation");
        result.put("promptTaskCode", "auto-prompt-governance");
        result.put("skipReportTask", "false");
        result.put("allowPromptCandidate", "true");
        result.put("allowModelCandidate", "true");
        result.put("requireStructuredCoreData", "false");
        result.put("minQualityScore", "0.45");
        result.put("maxReportsForMock", "20");
        result.put("allowAutoMockTrade", "true");
        result.put("allowAutoPromptActivation", "false");
        result.put("allowAutoModelActivation", "false");
        result.put("allowRealTrade", "false");
        result.put("maxSingleTradeAmount", "10000");
        result.put("benchmarkCode", "");
        result.put("valuationPointLimit", "100");
        return result;
    }

    /**
     * 将自动闭环配置方案合并到任务参数。
     *
     * @param baseParameters 任务定义或触发参数
     * @param requestedProfileCode 显式选择的方案编码
     * @return 已合并方案参数和方案快照的任务参数
     * @author dz
     * @date 2026-06-30
     */
    public Map<String, String> applyAutoClosedLoopProfile(Map<String, String> baseParameters, String requestedProfileCode) {
        Map<String, String> parameters = new LinkedHashMap<>(baseParameters == null ? Map.of() : baseParameters);
        String profileCode = requestedProfileCode == null || requestedProfileCode.isBlank()
            ? parameters.get("configProfileCode")
            : requestedProfileCode.trim();
        if (profileCode == null || profileCode.isBlank()) {
            return parameters;
        }
        Map<String, String> profileParameters = profileParameters(profileCode)
            .orElseThrow(() -> new com.example.dzcom.application.common.exception.BusinessException(
                org.springframework.http.HttpStatus.NOT_FOUND, "自动闭环配置方案不存在: " + profileCode));
        parameters.putAll(profileParameters);
        parameters.put("configProfileCode", profileCode);
        parameters.put("configProfileSnapshot", Jsons.toJson(profileParameters));
        return parameters;
    }

    /**
     * 把高级方案 JSON 展开成闭环处理器可消费的扁平任务参数。
     *
     * @param values 原始方案 JSON Map
     * @return 扁平任务参数
     * @author dz
     * @date 2026-06-30
     */
    private Map<String, String> flattenProfileParameters(Map<String, Object> values) {
        Map<String, String> result = stringMap(values);
        mapNestedString(values, result, "execution", "runMode", "runMode");
        mapNestedString(values, result, "execution", "marketScope", "marketScope");
        mapNestedList(values, result, "execution", "dataTaskCodes", "dataTaskCodes");
        mapNestedString(values, result, "execution", "reportTaskCode", "reportTaskCode");
        mapNestedString(values, result, "execution", "promptTaskCode", "promptTaskCode");
        mapNestedBool(values, result, "execution", "skipReportTask", "skipReportTask");
        mapNestedBool(values, result, "execution", "allowPromptCandidate", "allowPromptCandidate");
        mapNestedBool(values, result, "execution", "allowModelCandidate", "allowModelCandidate");
        mapNestedBool(values, result, "qualityGate", "requireStructuredCoreData", "requireStructuredCoreData");
        mapNestedString(values, result, "qualityGate", "minQualityScore", "minQualityScore");
        mapNestedString(values, result, "qualityGate", "maxReportsForMock", "maxReportsForMock");
        mapNestedBool(values, result, "safety", "allowAutoMockTrade", "allowAutoMockTrade");
        mapNestedBool(values, result, "safety", "allowAutoPromptActivation", "allowAutoPromptActivation");
        mapNestedBool(values, result, "safety", "allowAutoModelActivation", "allowAutoModelActivation");
        mapNestedBool(values, result, "safety", "allowRealTrade", "allowRealTrade");
        mapNestedString(values, result, "safety", "maxSingleTradeAmount", "maxSingleTradeAmount");
        mapNestedString(values, result, "backtest", "benchmarkCode", "benchmarkCode");
        mapNestedString(values, result, "backtest", "valuationPointLimit", "valuationPointLimit");
        return result;
    }

    /**
     * 保留方案中的顶层标量与 JSON 字段。
     *
     * @param values 原始方案 JSON Map
     * @return 顶层字段字符串 Map
     * @author dz
     * @date 2026-06-30
     */
    private Map<String, String> stringMap(Map<String, Object> values) {
        Map<String, String> result = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (key != null && value != null) {
                result.put(key, value instanceof String text ? text : Jsons.toJson(value));
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    /**
     * 读取方案中的嵌套分组。
     *
     * @param values 原始方案 JSON Map
     * @param group 分组名称
     * @return 分组 Map；不存在时为空
     * @author dz
     * @date 2026-06-30
     */
    private Map<String, Object> nested(Map<String, Object> values, String group) {
        Object value = values.get(group);
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    /**
     * 映射嵌套字符串字段到扁平参数。
     *
     * @param values 原始方案 JSON Map
     * @param result 扁平参数目标 Map
     * @param group 分组名称
     * @param sourceKey 源字段名
     * @param targetKey 目标参数名
     * @author dz
     * @date 2026-06-30
     */
    private void mapNestedString(Map<String, Object> values, Map<String, String> result,
                                 String group, String sourceKey, String targetKey) {
        Object value = nested(values, group).get(sourceKey);
        if (value != null && !String.valueOf(value).isBlank()) {
            result.put(targetKey, String.valueOf(value).trim());
        }
    }

    /**
     * 映射嵌套布尔字段到扁平参数。
     *
     * @param values 原始方案 JSON Map
     * @param result 扁平参数目标 Map
     * @param group 分组名称
     * @param sourceKey 源字段名
     * @param targetKey 目标参数名
     * @author dz
     * @date 2026-06-30
     */
    private void mapNestedBool(Map<String, Object> values, Map<String, String> result,
                               String group, String sourceKey, String targetKey) {
        Object value = nested(values, group).get(sourceKey);
        if (value != null) {
            result.put(targetKey, String.valueOf(value));
        }
    }

    /**
     * 映射嵌套列表字段到逗号分隔任务参数。
     *
     * @param values 原始方案 JSON Map
     * @param result 扁平参数目标 Map
     * @param group 分组名称
     * @param sourceKey 源字段名
     * @param targetKey 目标参数名
     * @author dz
     * @date 2026-06-30
     */
    private void mapNestedList(Map<String, Object> values, Map<String, String> result,
                               String group, String sourceKey, String targetKey) {
        Object value = nested(values, group).get(sourceKey);
        if (value instanceof List<?> list) {
            result.put(targetKey, String.join(",", list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList()));
        } else if (value != null && !String.valueOf(value).isBlank()) {
            result.put(targetKey, String.valueOf(value).trim());
        }
    }

    /**
     * 读取自动闭环系统配置字符串值。
     *
     * @param key 配置键
     * @param fallback 兜底值
     * @return 配置值或兜底值
     * @author dz
     * @date 2026-06-30
     */
    private String stringValue(String key, String fallback) {
        return configs.stringValue(CONFIG_GROUP, key)
            .filter(value -> !value.isBlank())
            .orElse(fallback);
    }
}
