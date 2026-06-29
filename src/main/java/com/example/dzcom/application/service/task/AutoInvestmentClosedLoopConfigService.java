package com.example.dzcom.application.service.task;

import com.example.dzcom.application.service.system.SystemConfigReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** 自动投资闭环配置解析服务。 */
@Service
@RequiredArgsConstructor
public class AutoInvestmentClosedLoopConfigService {
    private static final String CONFIG_GROUP = "AUTO_INVESTMENT_CLOSED_LOOP";
    private static final String DEFAULT_AUTOMATION_LEVEL = "FULL_MOCK";
    private static final String DEFAULT_MOCK_USER_BIZ_ID = "21000000-0000-0000-0000-000000000002";
    private static final String DEFAULT_MOCK_PORTFOLIO_NAME = "全自动闭环模拟组合";
    private static final BigDecimal DEFAULT_INITIAL_CASH = new BigDecimal("100000");
    private static final String DEFAULT_PROMPT_CODE = "investment-plan-from-report";
    private static final String DEFAULT_PROMPT_VERSION = "auto-v1";
    private static final String DEFAULT_PROMPT_SCENARIO = "INVESTMENT_PLAN";
    private static final String DEFAULT_MODEL_TYPE = "INVESTMENT_ANALYSIS";

    private final SystemConfigReader configs;

    public String automationLevel() {
        return stringValue("automationLevel", DEFAULT_AUTOMATION_LEVEL);
    }

    public String mockUserBizId() {
        return stringValue("mockUserBizId", DEFAULT_MOCK_USER_BIZ_ID);
    }

    public String mockPortfolioName() {
        return stringValue("mockPortfolioName", DEFAULT_MOCK_PORTFOLIO_NAME);
    }

    public BigDecimal initialCash() {
        return configs.decimalValue(CONFIG_GROUP, "initialCash")
            .filter(value -> value.compareTo(BigDecimal.ZERO) > 0)
            .orElse(DEFAULT_INITIAL_CASH);
    }

    public String promptCode() {
        return stringValue("promptCode", DEFAULT_PROMPT_CODE);
    }

    public String promptVersion() {
        return stringValue("promptVersion", DEFAULT_PROMPT_VERSION);
    }

    public String promptScenario() {
        return stringValue("promptScenario", DEFAULT_PROMPT_SCENARIO);
    }

    public String modelType() {
        return stringValue("modelType", DEFAULT_MODEL_TYPE);
    }

    private String stringValue(String key, String fallback) {
        return configs.stringValue(CONFIG_GROUP, key)
            .filter(value -> !value.isBlank())
            .orElse(fallback);
    }
}
