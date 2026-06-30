package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.service.system.SystemConfigReader;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.model.task.InvestmentThemeOption;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.NewsArticleRelation;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleRelationSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleRelationStore;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionSearchCriteria;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 投资任务管理服务测试。 */
class InvestmentTaskManagementServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 29, 23, 30);

    @Test
    void triggerAutoClosedLoopMergesSelectedConfigProfileSnapshot() {
        MemoryDefinitionStore definitions = new MemoryDefinitionStore();
        definitions.save(InvestmentTaskDefinition.builder()
            .bizId("task-1")
            .taskCode("auto-investment-closed-loop-orchestration")
            .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
            .cron("0 * * * * *")
            .zone("Asia/Shanghai")
            .enabled(true)
            .parameters(new LinkedHashMap<>(Map.of("allowAutoMockTrade", "true")))
            .description("auto")
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
        MemorySystemConfigReader configs = new MemorySystemConfigReader();
        configs.putString("AUTO_INVESTMENT_CLOSED_LOOP_PROFILE", "strategy-a",
            "{\"profileCode\":\"strategy-a\",\"mockPortfolioBizId\":\"portfolio-a\",\"promptCode\":\"prompt-a\",\"initialCash\":\"200000\"}");
        CapturingTriggerPort triggerPort = new CapturingTriggerPort();
        InvestmentTaskManagementService service = new InvestmentTaskManagementService(
            definitions,
            triggerPort,
            new AutoInvestmentClosedLoopConfigService(configs),
            new EmptyExecutionStore(),
            new EmptyArticleStore(),
            new EmptyRelationStore(),
            new EmptySnapshotStore(),
            new FixedIdGenerator(),
            () -> NOW
        );

        service.trigger("auto-investment-closed-loop-orchestration", Map.of(
            "configProfileCode", "strategy-a",
            "mockProductBizId", "product-1"
        ), "MANUAL");

        Map<String, String> parameters = triggerPort.lastEvent.parameters();
        assertEquals("strategy-a", parameters.get("configProfileCode"));
        assertEquals("portfolio-a", parameters.get("mockPortfolioBizId"));
        assertEquals("prompt-a", parameters.get("promptCode"));
        assertEquals("product-1", parameters.get("mockProductBizId"));
        assertTrue(parameters.get("configProfileSnapshot").contains("\"mockPortfolioBizId\":\"portfolio-a\""));
    }

    @Test
    void triggerAutoClosedLoopCanUseDefaultProfileFallbackWhenDatabaseSeedMissing() {
        MemoryDefinitionStore definitions = new MemoryDefinitionStore();
        definitions.save(InvestmentTaskDefinition.builder()
            .bizId("task-1")
            .taskCode("auto-investment-closed-loop-orchestration")
            .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
            .cron("0 * * * * *")
            .zone("Asia/Shanghai")
            .enabled(true)
            .parameters(new LinkedHashMap<>())
            .description("auto")
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
        CapturingTriggerPort triggerPort = new CapturingTriggerPort();
        InvestmentTaskManagementService service = new InvestmentTaskManagementService(
            definitions,
            triggerPort,
            new AutoInvestmentClosedLoopConfigService(new MemorySystemConfigReader()),
            new EmptyExecutionStore(),
            new EmptyArticleStore(),
            new EmptyRelationStore(),
            new EmptySnapshotStore(),
            new FixedIdGenerator(),
            () -> NOW
        );

        service.trigger("auto-investment-closed-loop-orchestration", Map.of(
            "configProfileCode", "default-auto-mock"
        ), "MANUAL");

        Map<String, String> parameters = triggerPort.lastEvent.parameters();
        assertEquals("default-auto-mock", parameters.get("configProfileCode"));
        assertEquals("FULL_MOCK", parameters.get("automationLevel"));
        assertEquals("全自动闭环模拟组合", parameters.get("mockPortfolioName"));
        assertTrue(parameters.get("configProfileSnapshot").contains("\"profileCode\":\"default-auto-mock\""));
    }

    @Test
    void triggerAutoClosedLoopFlattensAdvancedConfigProfile() {
        MemoryDefinitionStore definitions = new MemoryDefinitionStore();
        definitions.save(InvestmentTaskDefinition.builder()
            .bizId("task-1")
            .taskCode("auto-investment-closed-loop-orchestration")
            .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
            .cron("0 * * * * *")
            .zone("Asia/Shanghai")
            .enabled(true)
            .parameters(new LinkedHashMap<>(Map.of("allowAutoMockTrade", "true")))
            .description("auto")
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
        MemorySystemConfigReader configs = new MemorySystemConfigReader();
        configs.putString("AUTO_INVESTMENT_CLOSED_LOOP_PROFILE", "advanced-a", """
            {
              "profileCode":"advanced-a",
              "profileType":"SCHEDULED_BASELINE",
              "riskLevel":"LOW",
              "execution":{"runMode":"FULL_PIPELINE","marketScope":"CN_MAINLAND","dataTaskCodes":["quality-task","news-task"],"reportTaskCode":"report-task","skipReportTask":false},
              "qualityGate":{"minQualityScore":"0.70","maxReportsForMock":"12","requireStructuredCoreData":true},
              "safety":{"allowAutoMockTrade":true,"allowAutoPromptActivation":false,"maxSingleTradeAmount":"8000"},
              "backtest":{"benchmarkCode":"CSI300","valuationPointLimit":"120"}
            }
            """);
        CapturingTriggerPort triggerPort = new CapturingTriggerPort();
        InvestmentTaskManagementService service = new InvestmentTaskManagementService(
            definitions,
            triggerPort,
            new AutoInvestmentClosedLoopConfigService(configs),
            new EmptyExecutionStore(),
            new EmptyArticleStore(),
            new EmptyRelationStore(),
            new EmptySnapshotStore(),
            new FixedIdGenerator(),
            () -> NOW
        );

        service.trigger("auto-investment-closed-loop-orchestration", Map.of("configProfileCode", "advanced-a"), "MANUAL");

        Map<String, String> parameters = triggerPort.lastEvent.parameters();
        assertEquals("FULL_PIPELINE", parameters.get("runMode"));
        assertEquals("quality-task,news-task", parameters.get("dataTaskCodes"));
        assertEquals("report-task", parameters.get("reportTaskCode"));
        assertEquals("0.70", parameters.get("minQualityScore"));
        assertEquals("12", parameters.get("maxReportsForMock"));
        assertEquals("true", parameters.get("requireStructuredCoreData"));
        assertEquals("false", parameters.get("allowAutoPromptActivation"));
        assertEquals("8000", parameters.get("maxSingleTradeAmount"));
        assertEquals("CSI300", parameters.get("benchmarkCode"));
        assertTrue(parameters.get("configProfileSnapshot").contains("\"qualityGate\""));
    }

    @Test
    void scheduledAutoClosedLoopUsesConfiguredAuthoritativeProfile() {
        MemoryDefinitionStore definitions = new MemoryDefinitionStore();
        definitions.save(InvestmentTaskDefinition.builder()
            .bizId("task-1")
            .taskCode("auto-investment-closed-loop-orchestration")
            .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
            .cron("0 * * * * *")
            .zone("Asia/Shanghai")
            .enabled(true)
            .parameters(new LinkedHashMap<>(Map.of(
                "allowAutoMockTrade", "true",
                "configProfileCode", "stale-task-profile",
                "mockPortfolioBizId", "stale-portfolio"
            )))
            .description("auto")
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
        MemorySystemConfigReader configs = new MemorySystemConfigReader();
        configs.putString("AUTO_INVESTMENT_CLOSED_LOOP", "scheduledConfigProfileCode", "schedule-default");
        configs.putString("AUTO_INVESTMENT_CLOSED_LOOP_PROFILE", "schedule-default",
            "{\"profileCode\":\"schedule-default\",\"mockPortfolioBizId\":\"schedule-portfolio\",\"promptCode\":\"schedule-prompt\"}");
        CapturingTriggerPort triggerPort = new CapturingTriggerPort();
        InvestmentTaskManagementService service = new InvestmentTaskManagementService(
            definitions,
            triggerPort,
            new AutoInvestmentClosedLoopConfigService(configs),
            new EmptyExecutionStore(),
            new EmptyArticleStore(),
            new EmptyRelationStore(),
            new EmptySnapshotStore(),
            new FixedIdGenerator(),
            () -> NOW
        );

        service.trigger("auto-investment-closed-loop-orchestration", Map.of(), "SCHEDULE");

        Map<String, String> parameters = triggerPort.lastEvent.parameters();
        assertEquals("schedule-default", parameters.get("configProfileCode"));
        assertEquals("schedule-portfolio", parameters.get("mockPortfolioBizId"));
        assertEquals("schedule-prompt", parameters.get("promptCode"));
        assertEquals("true", parameters.get("allowAutoMockTrade"));
        assertTrue(parameters.get("configProfileSnapshot").contains("\"mockPortfolioBizId\":\"schedule-portfolio\""));
    }

    private static final class MemoryDefinitionStore implements InvestmentTaskDefinitionStore {
        private final Map<String, InvestmentTaskDefinition> definitions = new LinkedHashMap<>();

        @Override
        public List<InvestmentTaskDefinition> findAll() {
            return List.copyOf(definitions.values());
        }

        @Override
        public Optional<InvestmentTaskDefinition> findByCode(String taskCode) {
            return Optional.ofNullable(definitions.get(taskCode));
        }

        @Override
        public InvestmentTaskDefinition save(InvestmentTaskDefinition definition) {
            definitions.put(definition.taskCode(), definition);
            return definition;
        }
    }

    private static final class CapturingTriggerPort implements InvestmentTaskTriggerPort {
        private InvestmentTaskEvent lastEvent;

        @Override
        public String publish(InvestmentTaskEvent event) {
            lastEvent = event;
            return event.eventId();
        }
    }

    private static final class MemorySystemConfigReader implements SystemConfigReader {
        private final Map<String, String> stringValues = new LinkedHashMap<>();

        private void putString(String group, String key, String value) {
            stringValues.put(group + ":" + key, value);
        }

        @Override
        public Optional<String> stringValue(String configGroup, String configKey) {
            return Optional.ofNullable(stringValues.get(configGroup + ":" + configKey));
        }

        @Override
        public Optional<BigDecimal> decimalValue(String configGroup, String configKey) {
            return Optional.empty();
        }
    }

    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public String newBizId() {
            return "event-1";
        }

        @Override
        public String newUserNo() {
            return "U0001";
        }
    }

    private static final class EmptyExecutionStore implements ScheduledTaskExecutionStore {
        @Override
        public ScheduledTaskExecution save(ScheduledTaskExecution execution) {
            return execution;
        }

        @Override
        public Optional<ScheduledTaskExecution> findByEventId(String eventId) {
            return Optional.empty();
        }

        @Override
        public PageResult<ScheduledTaskExecution> search(ScheduledTaskExecutionSearchCriteria criteria) {
            return emptyPage(criteria.page(), criteria.size());
        }
    }

    private static final class EmptyArticleStore implements NewsArticleStore {
        @Override
        public NewsArticle save(NewsArticle article) {
            return article;
        }

        @Override
        public long countByKeywords(List<String> keywords, LocalDateTime from) {
            return 0;
        }

        @Override
        public List<NewsArticle> findRecentByKeywords(List<String> keywords, LocalDateTime from, int limit) {
            return List.of();
        }

        @Override
        public PageResult<NewsArticle> search(NewsArticleSearchCriteria criteria) {
            return emptyPage(criteria.page(), criteria.size());
        }
    }

    private static final class EmptyRelationStore implements NewsArticleRelationStore {
        @Override
        public void saveBatch(List<NewsArticleRelation> relations) {
            // no-op
        }

        @Override
        public PageResult<NewsArticleRelation> search(NewsArticleRelationSearchCriteria criteria) {
            return emptyPage(criteria.page(), criteria.size());
        }
    }

    private static final class EmptySnapshotStore implements InvestmentThemeSnapshotStore {
        @Override
        public InvestmentThemeSnapshot save(InvestmentThemeSnapshot snapshot) {
            return snapshot;
        }

        @Override
        public PageResult<InvestmentThemeSnapshot> search(InvestmentThemeSnapshotSearchCriteria criteria) {
            return emptyPage(criteria.page(), criteria.size());
        }

        @Override
        public PageResult<InvestmentThemeOption> searchThemeOptions(String keyword, String marketScope, int page, int size) {
            return emptyPage(page, size);
        }
    }

    private static <T> PageResult<T> emptyPage(int page, int size) {
        return PageResult.<T>builder()
            .items(List.of())
            .total(0)
            .page(page)
            .size(size)
            .totalPages(0)
            .build();
    }
}
