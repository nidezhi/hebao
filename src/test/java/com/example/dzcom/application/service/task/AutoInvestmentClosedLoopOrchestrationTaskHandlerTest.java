package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockRebalanceCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.ai.BacktestResultView;
import com.example.dzcom.application.dto.ai.InvestmentFeedbackView;
import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockOrderView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.dto.portfolio.MockRebalanceExecutionView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.ai.AiModelApplicationService;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.application.service.system.SystemConfigReader;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.AiModelSkillBinding;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductInvestmentProfile;
import com.example.dzcom.domain.repository.product.ProductInvestmentProfileStore;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.ClosedLoopStep;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.ai.AiModelSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelSkillBindingStore;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.task.ClosedLoopRunSearchCriteria;
import com.example.dzcom.domain.repository.task.ClosedLoopRunStore;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionSearchCriteria;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 自动投资闭环总编排测试。 */
class AutoInvestmentClosedLoopOrchestrationTaskHandlerTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 25, 10, 0);

    /** 最近报告质量不达标时，应阻断闭环并且不进入 Mock 交易。 */
    @Test
    void shouldBlockWhenLatestReportDoesNotPassQualityGate() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(report("report-low", new BigDecimal("0.20"), false));

        assertThrows(RuntimeException.class, () -> fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-1")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "reportTaskCode", "report-task",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build()));

        assertEquals(0, fixture.portfolioService.buyFromReportCalls);
        assertEquals(0, fixture.portfolioService.rebalanceCalls);
        assertEquals("BLOCKED", fixture.closedLoopStore.runs.get(fixture.closedLoopStore.runs.size() - 1).runStatus());
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "QUALITY_GATE".equals(step.stepCode()) && "BLOCKED".equals(step.stepStatus())));
    }

    /** 最近一份报告不合格时，应继续在候选窗口内选择更早的合格报告。 */
    @Test
    void shouldSelectEarlierQualifiedReportWithinDefaultCandidateWindow() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(report("report-low", new BigDecimal("0.20"), false));
        fixture.reports.items.add(report("report-pass", new BigDecimal("0.80"), true));

        String summary = fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-2")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "reportTaskCode", "report-task",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertTrue(summary.contains("reportBizId=report-pass"));
        assertEquals(1, fixture.portfolioService.buyFromReportCalls);
        assertEquals("report-pass", fixture.portfolioService.lastReportBizId);
        assertTrue(fixture.reportTaskHandler.lastParameters.get("portfolioContext").contains("\"portfolioBizId\""));
        assertEquals(1, fixture.closedLoopService.backtestCalls);
        assertEquals(1, fixture.closedLoopService.feedbackCalls);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "QUALITY_GATE".equals(step.stepCode()) && "SUCCEEDED".equals(step.stepStatus())));
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "PROMPT_ACTIVATION_GUARD".equals(step.stepCode()) && "SUCCEEDED".equals(step.stepStatus())));
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "MODEL_ACTIVATION_GUARD".equals(step.stepCode()) && "SUCCEEDED".equals(step.stepStatus())));
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "REAL_TRADE_GUARD".equals(step.stepCode()) && "SKIPPED".equals(step.stepStatus())));
        assertTrue(fixture.modelStore.items.values().stream()
            .anyMatch(model -> model.modelVersion().startsWith("candidate-") && "ACTIVE".equals(model.status())));
    }

    /** 报告给出目标权重时，自动闭环应按组合上下文执行再平衡而不是固定买入。 */
    @Test
    void shouldRebalanceWhenReportProvidesTargetWeights() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(reportWithPlan("report-rebalance", new BigDecimal("0.80"), true, """
            {"planType":"REFERENCE_ALLOCATION","actionType":"REBALANCE","targetWeights":[{"productBizId":"product-a","targetWeight":0.35}]}
            """));

        fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-rebalance")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertEquals(1, fixture.portfolioService.rebalanceCalls);
        assertEquals(0, fixture.portfolioService.buyFromReportCalls);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "MOCK_TRADE".equals(step.stepCode())
                && step.outputSummary().contains("\"executionMode\":\"REBALANCE\"")));
    }

    /** 买入现金不足应记录为 Mock 决策阻断，不应落入 UNEXPECTED_FAILURE。 */
    @Test
    void shouldBlockMockTradeGracefullyWhenBuyPlanHasNoCash() {
        Fixture fixture = new Fixture();
        fixture.portfolioService.failBuyFromReport = true;
        fixture.reports.items.add(reportWithPlan("report-buy", new BigDecimal("0.80"), true, """
            {"planType":"REFERENCE_ALLOCATION","actionType":"BUY","referenceAllocationAmount":1000000}
            """));

        assertThrows(RuntimeException.class, () -> fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-no-cash")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build()));

        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "MOCK_TRADE".equals(step.stepCode())
                && "BLOCKED".equals(step.stepStatus())
                && step.failureReason().contains("Mock 计划无法执行")));
        assertTrue(fixture.closedLoopStore.steps.stream()
            .noneMatch(step -> "UNEXPECTED_FAILURE".equals(step.stepCode())));
    }

    /** 报告建议持有但组合空仓且质量达标时，默认执行小额探索买入产出可复盘样本。 */
    @Test
    void shouldExecuteExploratoryBuyWhenHoldReportHasNoPosition() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(reportWithPlan("report-hold", new BigDecimal("0.80"), true, """
            {"planType":"REFERENCE_ALLOCATION","actionType":"HOLD","decisionReason":"等待更好机会"}
            """));

        String summary = fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-hold")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertTrue(summary.contains("reportBizId=report-hold"));
        assertEquals(1, fixture.portfolioService.buyFromReportCalls);
        assertEquals(0, fixture.portfolioService.rebalanceCalls);
        assertEquals("product-a", fixture.portfolioService.lastProductBizId);
        assertEquals(new BigDecimal("5000.00"), fixture.portfolioService.lastMaxTradeAmount);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "MOCK_PORTFOLIO_CONTEXT".equals(step.stepCode())
                && step.outputSummary() != null
                && step.outputSummary().contains("\"candidateProducts\"")));
        assertEquals(1, fixture.closedLoopService.backtestCalls);
        assertEquals(1, fixture.closedLoopService.feedbackCalls);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .noneMatch(step -> "MOCK_TRADE_NOOP".equals(step.stepCode())));
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "MOCK_TRADE".equals(step.stepCode())
                && step.outputSummary() != null
                && step.outputSummary().contains("\"executionMode\":\"EXPLORATORY_BUY\"")));
    }

    /** 报告明确建议持有且关闭探索买入时，自动闭环应跳过下单但继续沉淀估值和反馈。 */
    @Test
    void shouldSkipOrderWhenReportSuggestsHoldAndExplorationDisabled() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(reportWithPlan("report-hold", new BigDecimal("0.80"), true, """
            {"planType":"REFERENCE_ALLOCATION","actionType":"HOLD","decisionReason":"现金不足，等待更好机会"}
            """));

        String summary = fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-hold-no-explore")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true",
                    "allowExploratoryMockBuy", "false"
                ))
                .triggeredAt(NOW)
                .build());

        assertTrue(summary.contains("reportBizId=report-hold"));
        assertEquals(0, fixture.portfolioService.buyFromReportCalls);
        assertEquals(0, fixture.portfolioService.rebalanceCalls);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "MOCK_TRADE_NOOP".equals(step.stepCode())
                && "SKIPPED".equals(step.stepStatus())
                && step.failureReason().contains("HOLD")));
    }

    /** 已有合格报告时可跳过报告生成，避免调试后半段闭环时重复消耗远端模型。 */
    @Test
    void shouldSkipReportTaskWhenReusableQualifiedReportExists() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(report("report-pass", new BigDecimal("0.80"), true));

        String summary = fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-skip-report")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "reportTaskCode", "report-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertTrue(summary.contains("reportBizId=report-pass"));
        assertEquals(0, fixture.reportTaskHandler.calls);
        assertEquals(1, fixture.portfolioService.buyFromReportCalls);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "REPORT_GENERATION".equals(step.stepCode()) && "SKIPPED".equals(step.stepStatus())));
    }

    /** Prompt 候选节点应写入可被前端追溯到 Prompt 配置页的字段。 */
    @Test
    void shouldPersistPromptTraceFieldsWhenPromptCandidateRuns() {
        Fixture fixture = new Fixture();
        fixture.reports.items.add(report("report-pass", new BigDecimal("0.80"), true));

        fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-prompt-candidate")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.ofEntries(
                    Map.entry("automationLevel", "FULL_MOCK"),
                    Map.entry("mockUserBizId", "user-1"),
                    Map.entry("minQualityScore", "0.45"),
                    Map.entry("dataTaskCodes", "data-task"),
                    Map.entry("skipReportTask", "true"),
                    Map.entry("promptTaskCode", "prompt-task"),
                    Map.entry("promptCode", "investment-plan-from-report"),
                    Map.entry("promptVersion", "auto-v1"),
                    Map.entry("promptScenario", "INVESTMENT_PLAN"),
                    Map.entry("requireStructuredCoreData", "false"),
                    Map.entry("allowAutoMockTrade", "true")
                ))
                .triggeredAt(NOW)
                .build());

        ClosedLoopStep promptStep = fixture.closedLoopStore.steps.stream()
            .filter(step -> "PROMPT_CANDIDATE".equals(step.stepCode()))
            .findFirst()
            .orElseThrow();
        assertEquals("SUCCEEDED", promptStep.stepStatus());
        assertTrue(promptStep.outputSummary().contains("\"reportBizId\":\"report-pass\""));
        assertTrue(promptStep.outputSummary().contains("\"promptCode\":\"investment-plan-from-report\""));
        assertTrue(promptStep.outputSummary().contains("\"promptVersion\":\"auto-v1\""));
        assertTrue(promptStep.outputSummary().contains("\"scenario\":\"INVESTMENT_PLAN\""));
        assertEquals("investment-plan-from-report", fixture.promptTaskHandler.lastParameters.get("promptCode"));
    }

    /** 低成本验证只跑真实质量快照时，应能使用快照摘要中的产品、行情和资讯计数通过数据门禁。 */
    @Test
    void shouldUseRealQualitySnapshotCountsWhenOnlyQualityTaskRuns() {
        Fixture fixture = new Fixture();
        fixture.definitions.save(fixture.definition("quality-task", "REAL_DATA_QUALITY_SNAPSHOT"));
        fixture.taskHandlers.add(new SuccessfulTaskHandler(
            "REAL_DATA_QUALITY_SNAPSHOT",
            "真实数据质量快照完成: quality=0.8000, products=8, quoteReady=8, recentNews=4"
        ));
        fixture.reports.items.add(report("report-pass", new BigDecimal("0.80"), true));

        String summary = fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-quality-only")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "quality-task",
                    "skipReportTask", "true",
                    "minStructuredProductCount", "1",
                    "minStructuredNewsCount", "1",
                    "minStructuredQuoteCount", "1",
                    "minRealDataQualityScore", "0.60",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertTrue(summary.contains("reportBizId=report-pass"));
        assertEquals(1, fixture.portfolioService.buyFromReportCalls);
    }

    /** 未在任务参数中指定 mockUserBizId 时，应使用可配置的自动闭环默认值。 */
    @Test
    void shouldUseConfiguredMockUserWhenTaskParameterIsMissing() {
        Fixture fixture = new Fixture();
        fixture.systemConfigs.putString("mockUserBizId", "configured-auto-user");
        fixture.systemConfigs.putString("mockPortfolioName", "配置化闭环组合");
        fixture.systemConfigs.putDecimal("initialCash", new BigDecimal("200000"));
        fixture.reports.items.add(report("report-pass", new BigDecimal("0.80"), true));

        fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-config-default")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertEquals("configured-auto-user", fixture.portfolioService.lastUserBizId);
        assertEquals("配置化闭环组合", fixture.portfolioService.lastPortfolioName);
        assertEquals(0, new BigDecimal("200000").compareTo(fixture.portfolioService.lastInitialCash));
    }

    /** 配置了具体 Mock 组合时，应优先使用该组合而不是按用户和名称创建资金池。 */
    @Test
    void shouldUseConfiguredMockPortfolioWhenPresent() {
        Fixture fixture = new Fixture();
        fixture.systemConfigs.putString("mockUserBizId", "configured-auto-user");
        fixture.systemConfigs.putString("mockPortfolioBizId", "configured-portfolio");
        fixture.portfolioService.configuredPortfolio = MockPortfolioView.builder()
            .bizId("configured-portfolio")
            .ownerUserBizId("portfolio-owner")
            .portfolioName("策略 A 专用资金池")
            .status(1)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build();
        fixture.reports.items.add(report("report-pass", new BigDecimal("0.80"), true));

        fixture.handler.execute(
            InvestmentTaskEvent.builder()
                .eventId("event-config-portfolio")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "skipReportTask", "true",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertEquals(0, fixture.portfolioService.ensureCalls);
        assertEquals("configured-portfolio", fixture.portfolioService.lastPortfolioBizId);
        assertTrue(fixture.operatorProvider.runAsUserBizIds.contains("portfolio-owner"));
    }

    /** 质量门禁阻断应作为任务 BLOCKED 记录，不应被审计为系统失败。 */
    @Test
    void shouldPersistBlockedExecutionWhenClosedLoopGateBlocks() {
        Fixture fixture = new Fixture();
        InvestmentTaskExecutionService executionService = new InvestmentTaskExecutionService(
            List.of(fixture.handler),
            fixture.executions,
            Optional.of(fixture.definitions),
            Optional.empty(),
            fixture.ids,
            () -> NOW
        );

        ScheduledTaskExecution execution = executionService.executeAndReturn(
            InvestmentTaskEvent.builder()
                .eventId("event-3")
                .taskCode("auto-investment-closed-loop-orchestration")
                .taskType("AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION")
                .triggerSource("MANUAL")
                .parameters(Map.of(
                    "automationLevel", "FULL_MOCK",
                    "mockUserBizId", "user-1",
                    "minQualityScore", "0.45",
                    "dataTaskCodes", "data-task",
                    "reportTaskCode", "report-task",
                    "requireStructuredCoreData", "false",
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertEquals("BLOCKED", execution.status());
        assertEquals("没有找到满足质量门禁的最新投资报告", execution.failureReason());
    }

    /** 历史 Kafka 定时消息命中已禁用任务时，应直接跳过，避免继续执行旧高消费参数。 */
    @Test
    void shouldSkipDisabledScheduledTaskEvent() {
        Fixture fixture = new Fixture();
        fixture.definitions.save(InvestmentTaskDefinition.builder()
            .bizId("disabled-task-id")
            .taskCode("disabled-ai-task")
            .taskType("EXPENSIVE_AI_TASK")
            .cron("0 * * * * *")
            .zone("Asia/Shanghai")
            .enabled(false)
            .parameters(Map.of("dataTaskCodes", "old-expensive-task"))
            .description("disabled")
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
        CountingTaskHandler expensiveHandler = new CountingTaskHandler("EXPENSIVE_AI_TASK");
        InvestmentTaskExecutionService executionService = new InvestmentTaskExecutionService(
            List.of(expensiveHandler),
            fixture.executions,
            Optional.of(fixture.definitions),
            Optional.empty(),
            fixture.ids,
            () -> NOW
        );

        ScheduledTaskExecution execution = executionService.executeAndReturn(
            InvestmentTaskEvent.builder()
                .eventId("event-disabled")
                .taskCode("disabled-ai-task")
                .taskType("EXPENSIVE_AI_TASK")
                .triggerSource("SCHEDULE")
                .parameters(Map.of("dataTaskCodes", "stale-expensive-task"))
                .triggeredAt(NOW)
                .build());

        assertEquals("SKIPPED", execution.status());
        assertEquals(0, expensiveHandler.calls);
    }

    /** 历史手动消息早于任务定义更新时间时，也应跳过已禁用任务，防止旧Kafka积压继续烧模型。 */
    @Test
    void shouldSkipStaleManualEventForDisabledTask() {
        Fixture fixture = new Fixture();
        fixture.definitions.save(InvestmentTaskDefinition.builder()
            .bizId("disabled-manual-task-id")
            .taskCode("disabled-manual-ai-task")
            .taskType("EXPENSIVE_AI_TASK")
            .cron("0 * * * * *")
            .zone("Asia/Shanghai")
            .enabled(false)
            .parameters(Map.of("dataTaskCodes", "new-real-task"))
            .description("disabled")
            .createdAt(NOW)
            .updatedAt(NOW.plusMinutes(10))
            .build());
        CountingTaskHandler expensiveHandler = new CountingTaskHandler("EXPENSIVE_AI_TASK");
        InvestmentTaskExecutionService executionService = new InvestmentTaskExecutionService(
            List.of(expensiveHandler),
            fixture.executions,
            Optional.of(fixture.definitions),
            Optional.empty(),
            fixture.ids,
            () -> NOW
        );

        ScheduledTaskExecution execution = executionService.executeAndReturn(
            InvestmentTaskEvent.builder()
                .eventId("event-stale-manual")
                .taskCode("disabled-manual-ai-task")
                .taskType("EXPENSIVE_AI_TASK")
                .triggerSource("MANUAL")
                .parameters(Map.of("dataTaskCodes", "stale-expensive-task"))
                .triggeredAt(NOW)
                .build());

        assertEquals("SKIPPED", execution.status());
        assertEquals(0, expensiveHandler.calls);
    }

    private static InvestmentAnalysisReport report(String bizId, BigDecimal qualityScore, boolean gatePassed) {
        return reportWithPlan(bizId, qualityScore, gatePassed, "{\"planType\":\"REFERENCE_ALLOCATION\"}");
    }

    private static InvestmentAnalysisReport reportWithPlan(
        String bizId,
        BigDecimal qualityScore,
        boolean gatePassed,
        String investmentPlan
    ) {
        return InvestmentAnalysisReport.builder()
            .bizId(bizId)
            .requestId("request-" + bizId)
            .providerCode("OPENAI_COMPATIBLE")
            .modelCode("openai-compatible-analysis")
            .marketScope("CN_MAINLAND")
            .status("SUCCEEDED")
            .confidenceLevel("MEDIUM_CONFIDENCE")
            .dataQualityScore(qualityScore)
            .dataQualityGate("{\"passed\":" + gatePassed + "}")
            .investmentPlan(investmentPlan)
            .generatedAt(NOW)
            .createdAt(NOW)
            .build();
    }

    /** 测试夹具。 */
    private static final class Fixture {
        private final SequenceIdGenerator ids = new SequenceIdGenerator();
        private final MemoryTaskDefinitionStore definitions = new MemoryTaskDefinitionStore();
        private final MemoryExecutionStore executions = new MemoryExecutionStore();
        private final MemoryClosedLoopRunStore closedLoopStore = new MemoryClosedLoopRunStore();
        private final MemoryReportStore reports = new MemoryReportStore();
        private final MemoryAiModelStore modelStore = new MemoryAiModelStore();
        private final MemoryProductStore productStore = new MemoryProductStore();
        private final MemoryProductInvestmentProfileStore investmentProfiles = new MemoryProductInvestmentProfileStore();
        private final CountingPortfolioService portfolioService = new CountingPortfolioService();
        private final MemorySystemConfigReader systemConfigs = new MemorySystemConfigReader();
        private final AutoInvestmentClosedLoopConfigService autoInvestmentConfig =
            new AutoInvestmentClosedLoopConfigService(systemConfigs);
        private final FakeInvestmentClosedLoopService closedLoopService = new FakeInvestmentClosedLoopService();
        private final SuccessfulTaskHandler dataTaskHandler = new SuccessfulTaskHandler("DATA_TASK");
        private final SuccessfulTaskHandler reportTaskHandler = new SuccessfulTaskHandler("REPORT_TASK");
        private final SuccessfulTaskHandler promptTaskHandler = new SuccessfulTaskHandler("PROMPT_TASK");
        private final List<InvestmentTaskHandler> taskHandlers = new ArrayList<>();
        private final PassThroughOperatorProvider operatorProvider = new PassThroughOperatorProvider();
        private final AutoInvestmentClosedLoopOrchestrationTaskHandler handler;

        private Fixture() {
            definitions.save(definition("data-task", "DATA_TASK"));
            definitions.save(definition("report-task", "REPORT_TASK"));
            definitions.save(definition("prompt-task", "PROMPT_TASK"));
            taskHandlers.add(dataTaskHandler);
            taskHandlers.add(reportTaskHandler);
            taskHandlers.add(promptTaskHandler);
            InvestmentTaskExecutionService executionService = new InvestmentTaskExecutionService(
                taskHandlers,
                executions,
                Optional.of(definitions),
                Optional.empty(),
                ids,
                () -> NOW
            );
            ClosedLoopOrchestrationApplicationService closedLoops =
                new ClosedLoopOrchestrationApplicationService(closedLoopStore, ids, () -> NOW);
            handler = new AutoInvestmentClosedLoopOrchestrationTaskHandler(
                definitions,
                new FixedObjectProvider<>(executionService),
                autoInvestmentConfig,
                closedLoops,
                reports,
                portfolioService,
                productStore,
                investmentProfiles,
                closedLoopService,
                new AiModelApplicationService(modelStore, new MemoryAiModelSkillBindingStore(), ids, () -> NOW),
                operatorProvider,
                ids,
                () -> NOW
            );
        }

        private InvestmentTaskDefinition definition(String code, String type) {
            return InvestmentTaskDefinition.builder()
                .bizId(code + "-id")
                .taskCode(code)
                .taskType(type)
                .cron("0 * * * * *")
                .zone("Asia/Shanghai")
                .enabled(true)
                .parameters(new LinkedHashMap<>())
                .description(code)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
        }
    }

    /** 固定成功的子任务处理器。 */
    private static final class SuccessfulTaskHandler implements InvestmentTaskHandler {
        private final String taskType;
        private final String summary;
        private Map<String, String> lastParameters = Map.of();
        private int calls;

        private SuccessfulTaskHandler(String taskType) {
            this(taskType, "ok");
        }

        private SuccessfulTaskHandler(String taskType, String summary) {
            this.taskType = taskType;
            this.summary = summary;
        }

        @Override
        public boolean supports(String type) {
            return taskType.equals(type);
        }

        @Override
        public String execute(InvestmentTaskEvent event) {
            calls++;
            lastParameters = event.parameters() == null ? Map.of() : event.parameters();
            return summary;
        }
    }

    /** 统计调用次数的任务处理器。 */
    private static final class CountingTaskHandler implements InvestmentTaskHandler {
        private final String taskType;
        private int calls;

        private CountingTaskHandler(String taskType) {
            this.taskType = taskType;
        }

        @Override
        public boolean supports(String type) {
            return taskType.equals(type);
        }

        @Override
        public String execute(InvestmentTaskEvent event) {
            calls++;
            return "executed";
        }
    }

    /** 固定对象 Provider，避免测试拉起 Spring 容器。 */
    private record FixedObjectProvider<T>(T value) implements ObjectProvider<T> {
        @Override
        public T getObject(Object... args) {
            return value;
        }

        @Override
        public T getIfAvailable() {
            return value;
        }

        @Override
        public T getIfUnique() {
            return value;
        }

        @Override
        public T getObject() {
            return value;
        }

        @Override
        public Stream<T> stream() {
            return Stream.of(value);
        }
    }

    /** 透传操作者上下文。 */
    private static final class PassThroughOperatorProvider implements CurrentOperatorProvider {
        private final List<String> runAsUserBizIds = new ArrayList<>();

        @Override
        public CurrentOperator required() {
            return new CurrentOperator("user-1", "test", java.util.Set.of("USER"), java.util.Set.of());
        }

        @Override
        public <T> T callAs(CurrentOperator operator, java.util.function.Supplier<T> action) {
            runAsUserBizIds.add(operator.userBizId());
            return action.get();
        }
    }

    /** 内存产品仓储。 */
    private static final class MemoryProductStore implements ProductStore {
        private final List<Product> items = List.of(Product.builder()
            .bizId("product-a")
            .productNo("P001")
            .productCode("588000")
            .productName("科创50ETF")
            .productType(ProductType.ETF)
            .marketCode("SSE")
            .currency("CNY")
            .tradeStatus(ProductTradeStatus.TRADABLE)
            .riskLevel(3)
            .createdAt(NOW)
            .createdBy("test")
            .deleted(0)
            .build());

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public Optional<Product> findByBizId(String bizId) {
            return items.stream().filter(item -> item.getBizId().equals(bizId)).findFirst();
        }

        @Override
        public boolean existsByMarketAndCode(String marketCode, String productCode) {
            return findByMarketAndCode(marketCode, productCode).isPresent();
        }

        @Override
        public Optional<Product> findByMarketAndCode(String marketCode, String productCode) {
            return items.stream()
                .filter(item -> item.getMarketCode().equals(marketCode) && item.getProductCode().equals(productCode))
                .findFirst();
        }

        @Override
        public PageResult<Product> search(ProductSearchCriteria criteria) {
            return PageResult.<Product>builder()
                .items(items.stream()
                    .filter(item -> criteria.tradeStatus() == null || item.getTradeStatus() == criteria.tradeStatus())
                    .toList())
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(1)
                .build();
        }
    }

    /** 内存产品画像仓储。 */
    private static final class MemoryProductInvestmentProfileStore implements ProductInvestmentProfileStore {
        private final ProductInvestmentProfile profile = ProductInvestmentProfile.builder()
            .bizId("profile-a")
            .productBizId("product-a")
            .assetClass("ETF")
            .riskSummary("测试画像")
            .volatilityLevel("MEDIUM")
            .liquidityLevel("HIGH")
            .suitableRiskLevel(3)
            .mockTradable(true)
            .minHoldingDays(1)
            .dataQualityScore(new BigDecimal("0.80"))
            .createdAt(NOW)
            .updatedAt(NOW)
            .build();

        @Override
        public ProductInvestmentProfile save(ProductInvestmentProfile profile) {
            return profile;
        }

        @Override
        public Optional<ProductInvestmentProfile> findByProductBizId(String productBizId) {
            return profile.productBizId().equals(productBizId) ? Optional.of(profile) : Optional.empty();
        }
    }

    /** 统计组合服务，质量门禁阻断时不应被调用。 */
    private static final class CountingPortfolioService extends com.example.dzcom.application.service.portfolio.MockPortfolioApplicationService {
        private int ensureCalls;
        private String lastReportBizId;
        private String lastUserBizId;
        private String lastPortfolioBizId;
        private String lastProductBizId;
        private String lastPortfolioName;
        private BigDecimal lastInitialCash;
        private BigDecimal lastMaxTradeAmount;
        private MockPortfolioView configuredPortfolio;
        private int buyFromReportCalls;
        private int rebalanceCalls;
        private boolean failBuyFromReport;

        private CountingPortfolioService() {
            super(new AutoInvestmentClosedLoopConfigService(new MemorySystemConfigReader()),
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        }

        @Override
        public MockPortfolioView ensureAutomationPortfolio(String userBizId, String portfolioName, BigDecimal initialCash) {
            ensureCalls++;
            lastUserBizId = userBizId;
            lastPortfolioName = portfolioName;
            lastInitialCash = initialCash;
            return portfolio(userBizId);
        }

        @Override
        public java.util.Optional<MockPortfolioView> configuredAutomationPortfolio(String portfolioBizId) {
            if (configuredPortfolio == null || portfolioBizId == null || portfolioBizId.isBlank()) {
                return java.util.Optional.empty();
            }
            lastPortfolioBizId = portfolioBizId;
            return java.util.Optional.of(configuredPortfolio);
        }

        @Override
        public MockOrderExecutionView buyFromReport(ExecuteMockPlanFromReportCommand command) {
            buyFromReportCalls++;
            lastReportBizId = command.reportBizId();
            lastProductBizId = command.productBizId();
            lastMaxTradeAmount = command.maxTradeAmount();
            if (failBuyFromReport) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "模拟组合现金不足");
            }
            MockPortfolioView portfolio = configuredPortfolio == null ? portfolio("user-1") : configuredPortfolio;
            return MockOrderExecutionView.builder()
                .portfolio(portfolio)
                .order(MockOrderView.builder()
                    .bizId("order-1")
                    .portfolioBizId(portfolio.bizId())
                    .status("FILLED")
                    .build())
                .build();
        }

        @Override
        public MockRebalanceExecutionView rebalance(ExecuteMockRebalanceCommand command) {
            rebalanceCalls++;
            MockPortfolioView portfolio = configuredPortfolio == null ? portfolio("user-1") : configuredPortfolio;
            MockOrderExecutionView execution = MockOrderExecutionView.builder()
                .portfolio(portfolio)
                .order(MockOrderView.builder()
                    .bizId("rebalance-order-1")
                    .portfolioBizId(portfolio.bizId())
                    .status("FILLED")
                    .build())
                .build();
            return MockRebalanceExecutionView.builder()
                .executions(List.of(execution))
                .portfolio(portfolio)
                .build();
        }

        @Override
        public MockPortfolioView refreshValuation(String portfolioBizId) {
            return portfolio("user-1");
        }

        private MockPortfolioView portfolio(String userBizId) {
            return MockPortfolioView.builder()
                .bizId("portfolio-1")
                .ownerUserBizId(userBizId)
                .portfolioName("全自动闭环模拟组合")
                .baseCurrency("CNY")
                .latestValuation(com.example.dzcom.application.dto.portfolio.PortfolioValuationView.builder()
                    .baseCurrency("CNY")
                    .totalAsset(new BigDecimal("100000"))
                    .cashBalance(new BigDecimal("100000"))
                    .positionValue(BigDecimal.ZERO)
                    .totalCost(BigDecimal.ZERO)
                    .unrealizedProfit(BigDecimal.ZERO)
                    .realizedProfit(BigDecimal.ZERO)
                    .totalReturnRate(BigDecimal.ZERO)
                    .sourceCode("TEST")
                    .build())
                .positions(List.of())
                .status(1)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
        }
    }

    /** 内存系统配置读取器。 */
    private static final class MemorySystemConfigReader implements SystemConfigReader {
        private final Map<String, String> stringValues = new LinkedHashMap<>();
        private final Map<String, BigDecimal> decimalValues = new LinkedHashMap<>();

        private void putString(String key, String value) {
            stringValues.put("AUTO_INVESTMENT_CLOSED_LOOP:" + key, value);
        }

        private void putDecimal(String key, BigDecimal value) {
            decimalValues.put("AUTO_INVESTMENT_CLOSED_LOOP:" + key, value);
        }

        @Override
        public Optional<String> stringValue(String configGroup, String configKey) {
            return Optional.ofNullable(stringValues.get(configGroup + ":" + configKey));
        }

        @Override
        public Optional<BigDecimal> decimalValue(String configGroup, String configKey) {
            return Optional.ofNullable(decimalValues.get(configGroup + ":" + configKey));
        }
    }

    /** 自动闭环测试用的回测和反馈服务。 */
    private static final class FakeInvestmentClosedLoopService extends InvestmentClosedLoopApplicationService {
        private int backtestCalls;
        private int feedbackCalls;

        private FakeInvestmentClosedLoopService() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public BacktestResultView generateBacktestFromPortfolio(GenerateBacktestFromPortfolioCommand command) {
            backtestCalls++;
            return BacktestResultView.builder()
                .bizId("backtest-1")
                .ownerUserBizId("user-1")
                .strategyCode(command.strategyCode())
                .strategyVersion(command.strategyVersion())
                .startDate(LocalDate.of(2026, 6, 25))
                .endDate(LocalDate.of(2026, 6, 25))
                .status("SUCCEEDED")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
        }

        @Override
        public InvestmentFeedbackView saveFeedback(SaveInvestmentFeedbackCommand command) {
            feedbackCalls++;
            return InvestmentFeedbackView.builder()
                .bizId("feedback-1")
                .userBizId("user-1")
                .targetType(command.targetType())
                .targetBizId(command.targetBizId())
                .reportBizId(command.reportBizId())
                .backtestBizId(command.backtestBizId())
                .feedbackAction(command.feedbackAction())
                .createdAt(NOW)
                .build();
        }
    }

    /** 内存任务定义仓储。 */
    private static final class MemoryTaskDefinitionStore implements InvestmentTaskDefinitionStore {
        private final Map<String, InvestmentTaskDefinition> items = new LinkedHashMap<>();

        @Override
        public List<InvestmentTaskDefinition> findAll() {
            return new ArrayList<>(items.values());
        }

        @Override
        public Optional<InvestmentTaskDefinition> findByCode(String taskCode) {
            return Optional.ofNullable(items.get(taskCode));
        }

        @Override
        public InvestmentTaskDefinition save(InvestmentTaskDefinition definition) {
            items.put(definition.taskCode(), definition);
            return definition;
        }
    }

    /** 内存任务执行仓储。 */
    private static final class MemoryExecutionStore implements ScheduledTaskExecutionStore {
        private final Map<String, ScheduledTaskExecution> items = new LinkedHashMap<>();

        @Override
        public Optional<ScheduledTaskExecution> findByEventId(String eventId) {
            return Optional.ofNullable(items.get(eventId));
        }

        @Override
        public ScheduledTaskExecution save(ScheduledTaskExecution execution) {
            items.put(execution.eventId(), execution);
            return execution;
        }

        @Override
        public PageResult<ScheduledTaskExecution> search(ScheduledTaskExecutionSearchCriteria criteria) {
            return PageResult.<ScheduledTaskExecution>builder()
                .items(new ArrayList<>(items.values()))
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(items.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 内存闭环运行仓储。 */
    private static final class MemoryClosedLoopRunStore implements ClosedLoopRunStore {
        private final List<ClosedLoopRun> runs = new ArrayList<>();
        private final List<ClosedLoopStep> steps = new ArrayList<>();

        @Override
        public ClosedLoopRun saveRun(ClosedLoopRun run) {
            runs.removeIf(item -> item.bizId().equals(run.bizId()));
            runs.add(run);
            return run;
        }

        @Override
        public ClosedLoopStep saveStep(ClosedLoopStep step) {
            steps.removeIf(item -> item.bizId().equals(step.bizId()));
            steps.add(step);
            return step;
        }

        @Override
        public Optional<ClosedLoopRun> findRunByBizId(String bizId) {
            return runs.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public List<ClosedLoopStep> findStepsByRunBizId(String runBizId) {
            return steps.stream().filter(item -> item.runBizId().equals(runBizId)).toList();
        }

        @Override
        public PageResult<ClosedLoopRun> searchRuns(ClosedLoopRunSearchCriteria criteria) {
            return PageResult.<ClosedLoopRun>builder()
                .items(runs)
                .total(runs.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(runs.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 内存报告仓储。 */
    private static final class MemoryReportStore implements InvestmentAnalysisReportStore {
        private final List<InvestmentAnalysisReport> items = new ArrayList<>();

        @Override
        public InvestmentAnalysisReport save(InvestmentAnalysisReport report) {
            items.add(report);
            return report;
        }

        @Override
        public Optional<InvestmentAnalysisReport> findByBizId(String bizId) {
            return items.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public PageResult<InvestmentAnalysisReport> search(InvestmentAnalysisReportSearchCriteria criteria) {
            return PageResult.<InvestmentAnalysisReport>builder()
                .items(items)
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(items.isEmpty() ? 0 : 1)
                .build();
        }

        @Override
        public PageResult<InvestmentAnalysisReport> latest(int size) {
            return PageResult.<InvestmentAnalysisReport>builder()
                .items(items.stream().limit(size).toList())
                .total(items.size())
                .page(1)
                .size(size)
                .totalPages(items.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 内存模型仓储。 */
    private static final class MemoryAiModelStore implements AiModelStore {
        private final Map<String, AiModel> items = new LinkedHashMap<>();

        private MemoryAiModelStore() {
            save(AiModel.builder()
                .bizId("model-active-1")
                .modelCode("openai-compatible-analysis")
                .modelVersion("default-v1")
                .modelName("OpenAI Compatible Analysis")
                .modelType("INVESTMENT_ANALYSIS")
                .provider("OPENAI_COMPATIBLE")
                .artifactUri("")
                .modelConfig("""
                    {"model":"gpt-test","baseUrl":"https://example.test/v1","secretRef":"OPENAI_API_KEY","mockEnabled":false,"temperature":0.2,"timeoutSeconds":30}
                    """)
                .metrics("{}")
                .status("ACTIVE")
                .activatedAt(NOW.minusDays(1))
                .createdAt(NOW.minusDays(1))
                .updatedAt(NOW.minusDays(1))
                .build());
        }

        @Override
        public Optional<AiModel> findByBizId(String bizId) {
            return Optional.ofNullable(items.get(bizId));
        }

        @Override
        public Optional<AiModel> findByCodeAndVersion(String modelCode, String modelVersion) {
            return items.values().stream()
                .filter(item -> item.modelCode().equals(modelCode) && item.modelVersion().equals(modelVersion))
                .findFirst();
        }

        @Override
        public Optional<AiModel> findActiveByCode(String modelCode) {
            return items.values().stream()
                .filter(item -> item.modelCode().equals(modelCode) && "ACTIVE".equals(item.status()))
                .reduce((left, right) -> {
                    if (left.activatedAt() == null) {
                        return right;
                    }
                    if (right.activatedAt() == null) {
                        return left;
                    }
                    return right.activatedAt().isAfter(left.activatedAt()) ? right : left;
                });
        }

        @Override
        public AiModel save(AiModel model) {
            items.put(model.bizId(), model);
            return model;
        }

        @Override
        public PageResult<AiModel> search(AiModelSearchCriteria criteria) {
            return PageResult.<AiModel>builder()
                .items(new ArrayList<>(items.values()))
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(items.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 内存模型 Skill 绑定仓储。 */
    private static final class MemoryAiModelSkillBindingStore implements AiModelSkillBindingStore {
        @Override
        public AiModelSkillBinding save(AiModelSkillBinding binding) {
            return binding;
        }

        @Override
        public Optional<AiModelSkillBinding> findByBizId(String bizId) {
            return Optional.empty();
        }

        @Override
        public Optional<AiModelSkillBinding> findByModelSkillAndScenario(
            String modelBizId,
            String skillBizId,
            String scenarioCode
        ) {
            return Optional.empty();
        }

        @Override
        public List<AiModelSkillBinding> findEnabledByModelBizId(String modelBizId) {
            return List.of();
        }

        @Override
        public PageResult<AiModelSkillBinding> search(AiModelSkillBindingSearchCriteria criteria) {
            return PageResult.<AiModelSkillBinding>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }
    }

    /** 顺序业务 ID 生成器。 */
    private static final class SequenceIdGenerator implements IdGenerator {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public String newBizId() {
            return "00000000-0000-0000-0000-" + String.format("%012d", sequence.incrementAndGet());
        }

        @Override
        public String newUserNo() {
            return "U" + sequence.incrementAndGet();
        }
    }
}
