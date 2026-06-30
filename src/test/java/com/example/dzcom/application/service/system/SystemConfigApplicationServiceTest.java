package com.example.dzcom.application.service.system;

import com.example.dzcom.application.command.system.SaveSystemConfigCommand;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.system.SystemConfig;
import com.example.dzcom.domain.repository.system.SystemConfigSearchCriteria;
import com.example.dzcom.domain.repository.system.SystemConfigStore;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemConfigApplicationServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 29, 23, 0);

    @Test
    void saveNormalizesTypedValueAndReaderReturnsDisplayValues() {
        InMemorySystemConfigStore store = new InMemorySystemConfigStore();
        SystemConfigApplicationService service = new SystemConfigApplicationService(
            store,
            new FixedEnvironment("dev"),
            new FixedIdGenerator(),
            () -> NOW
        );

        service.save(SaveSystemConfigCommand.builder()
            .configGroup("auto_investment_closed_loop")
            .configKey("initialCash")
            .environment("dev")
            .valueType("number")
            .configValue("200000.50")
            .description("测试初始资金")
            .status("enabled")
            .build());

        assertTrue(service.decimalValue("AUTO_INVESTMENT_CLOSED_LOOP", "initialCash")
            .filter(value -> value.compareTo(new BigDecimal("200000.50")) == 0)
            .isPresent());
        assertEquals("200000.50", store.saved.get("AUTO_INVESTMENT_CLOSED_LOOP:initialCash:DEV").configValue());
    }

    @Test
    void stringReaderFallsBackToDefaultEnvironment() {
        InMemorySystemConfigStore store = new InMemorySystemConfigStore();
        store.save(SystemConfig.builder()
            .bizId("cfg-1")
            .configGroup("AUTO_INVESTMENT_CLOSED_LOOP")
            .configKey("mockPortfolioName")
            .environment("DEFAULT")
            .valueType("STRING")
            .configValue("\"全自动闭环模拟组合\"")
            .description("组合名")
            .status("ENABLED")
            .version(0)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
        SystemConfigApplicationService service = new SystemConfigApplicationService(
            store,
            new FixedEnvironment("prod"),
            new FixedIdGenerator(),
            () -> NOW
        );

        assertEquals(Optional.of("全自动闭环模拟组合"),
            service.stringValue("AUTO_INVESTMENT_CLOSED_LOOP", "mockPortfolioName"));
    }

    @Test
    void listReturnsConfiguredItems() {
        InMemorySystemConfigStore store = new InMemorySystemConfigStore();
        SystemConfigApplicationService service = new SystemConfigApplicationService(
            store,
            new FixedEnvironment(),
            new FixedIdGenerator(),
            () -> NOW
        );
        service.save(SaveSystemConfigCommand.builder()
            .configGroup("AUTO_INVESTMENT_CLOSED_LOOP")
            .configKey("automationLevel")
            .valueType("STRING")
            .configValue("FULL_MOCK")
            .status("ENABLED")
            .build());

        PageResult<?> page = service.list("AUTO_INVESTMENT_CLOSED_LOOP", null, "DEFAULT", null,
            new com.example.dzcom.application.common.page.PageQuery(1, 20, "configKey", "asc"));

        assertEquals(1, page.total());
        assertTrue(page.items().get(0).toString().contains("automationLevel"));
    }

    @Test
    void listAutoClosedLoopProfilesReturnsDefaultFallbackWhenDatabaseSeedMissing() {
        InMemorySystemConfigStore store = new InMemorySystemConfigStore();
        SystemConfigApplicationService service = new SystemConfigApplicationService(
            store,
            new FixedEnvironment(),
            new FixedIdGenerator(),
            () -> NOW
        );

        PageResult<?> page = service.list("AUTO_INVESTMENT_CLOSED_LOOP_PROFILE", null, "DEFAULT", "ENABLED",
            new com.example.dzcom.application.common.page.PageQuery(1, 20, "configKey", "asc"));

        assertEquals(1, page.total());
        assertTrue(page.items().get(0).toString().contains("default-auto-mock"));
    }

    private record FixedEnvironment(String... profiles) implements Environment {
        @Override
        public String[] getActiveProfiles() {
            return profiles;
        }

        @Override
        public String[] getDefaultProfiles() {
            return new String[0];
        }

        @Override
        public boolean acceptsProfiles(String... profiles) {
            return false;
        }

        @Override
        public boolean acceptsProfiles(Profiles profiles) {
            return false;
        }

        @Override
        public boolean containsProperty(String key) {
            return false;
        }

        @Override
        public String getProperty(String key) {
            return null;
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return defaultValue;
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType) {
            return null;
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            return defaultValue;
        }

        @Override
        public String getRequiredProperty(String key) {
            throw new IllegalStateException();
        }

        @Override
        public <T> T getRequiredProperty(String key, Class<T> targetType) {
            throw new IllegalStateException();
        }

        @Override
        public String resolvePlaceholders(String text) {
            return text;
        }

        @Override
        public String resolveRequiredPlaceholders(String text) {
            return text;
        }
    }

    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public String newBizId() {
            return "cfg-new";
        }

        @Override
        public String newUserNo() {
            return "U0001";
        }
    }

    private static final class InMemorySystemConfigStore implements SystemConfigStore {
        private final Map<String, SystemConfig> saved = new LinkedHashMap<>();

        @Override
        public Optional<SystemConfig> findEnabled(String configGroup, String configKey, String environment) {
            return findByKey(configGroup, configKey, environment)
                .filter(config -> "ENABLED".equals(config.status()));
        }

        @Override
        public Optional<SystemConfig> findByKey(String configGroup, String configKey, String environment) {
            return Optional.ofNullable(saved.get(key(configGroup, configKey, environment)));
        }

        @Override
        public SystemConfig save(SystemConfig config) {
            saved.put(key(config.configGroup(), config.configKey(), config.environment()), config);
            return config;
        }

        @Override
        public PageResult<SystemConfig> search(SystemConfigSearchCriteria criteria) {
            var items = saved.values().stream()
                .filter(config -> criteria.configGroup() == null || criteria.configGroup().equals(config.configGroup()))
                .filter(config -> criteria.environment() == null || criteria.environment().equals(config.environment()))
                .filter(config -> criteria.status() == null || criteria.status().equals(config.status()))
                .toList();
            return PageResult.<SystemConfig>builder()
                .items(items)
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(items.isEmpty() ? 0 : 1)
                .build();
        }

        private String key(String configGroup, String configKey, String environment) {
            return configGroup + ":" + configKey + ":" + environment;
        }
    }
}
