package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.dto.ai.BacktestResultView;
import com.example.dzcom.application.dto.ai.InvestmentFeedbackView;
import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockOrderView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.ai.AiModelApplicationService;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.AiModelSkillBinding;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
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
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build()));

        assertEquals(0, fixture.portfolioService.ensureCalls);
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
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertTrue(summary.contains("reportBizId=report-pass"));
        assertEquals(1, fixture.portfolioService.ensureCalls);
        assertEquals("report-pass", fixture.portfolioService.lastReportBizId);
        assertEquals(1, fixture.closedLoopService.backtestCalls);
        assertEquals(1, fixture.closedLoopService.feedbackCalls);
        assertTrue(fixture.closedLoopStore.steps.stream()
            .anyMatch(step -> "QUALITY_GATE".equals(step.stepCode()) && "SUCCEEDED".equals(step.stepStatus())));
    }

    /** 质量门禁阻断应作为任务 BLOCKED 记录，不应被审计为系统失败。 */
    @Test
    void shouldPersistBlockedExecutionWhenClosedLoopGateBlocks() {
        Fixture fixture = new Fixture();
        InvestmentTaskExecutionService executionService = new InvestmentTaskExecutionService(
            List.of(fixture.handler),
            fixture.executions,
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
                    "allowAutoMockTrade", "true"
                ))
                .triggeredAt(NOW)
                .build());

        assertEquals("BLOCKED", execution.status());
        assertEquals("没有找到满足质量门禁的最新投资报告", execution.failureReason());
    }

    private static InvestmentAnalysisReport report(String bizId, BigDecimal qualityScore, boolean gatePassed) {
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
            .investmentPlan("{\"planType\":\"REFERENCE_ALLOCATION\"}")
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
        private final CountingPortfolioService portfolioService = new CountingPortfolioService();
        private final FakeInvestmentClosedLoopService closedLoopService = new FakeInvestmentClosedLoopService();
        private final AutoInvestmentClosedLoopOrchestrationTaskHandler handler;

        private Fixture() {
            definitions.save(definition("data-task", "DATA_TASK"));
            definitions.save(definition("report-task", "REPORT_TASK"));
            InvestmentTaskExecutionService executionService = new InvestmentTaskExecutionService(
                List.of(new SuccessfulTaskHandler("DATA_TASK"), new SuccessfulTaskHandler("REPORT_TASK")),
                executions,
                ids,
                () -> NOW
            );
            ClosedLoopOrchestrationApplicationService closedLoops =
                new ClosedLoopOrchestrationApplicationService(closedLoopStore, ids, () -> NOW);
            handler = new AutoInvestmentClosedLoopOrchestrationTaskHandler(
                definitions,
                new FixedObjectProvider<>(executionService),
                closedLoops,
                reports,
                portfolioService,
                closedLoopService,
                new AiModelApplicationService(new MemoryAiModelStore(), new MemoryAiModelSkillBindingStore(), ids, () -> NOW),
                new PassThroughOperatorProvider(),
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
    private record SuccessfulTaskHandler(String taskType) implements InvestmentTaskHandler {
        @Override
        public boolean supports(String type) {
            return taskType.equals(type);
        }

        @Override
        public String execute(InvestmentTaskEvent event) {
            return "ok";
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
        @Override
        public CurrentOperator required() {
            return new CurrentOperator("user-1", "test", java.util.Set.of("USER"), java.util.Set.of());
        }

        @Override
        public <T> T callAs(CurrentOperator operator, java.util.function.Supplier<T> action) {
            return action.get();
        }
    }

    /** 统计组合服务，质量门禁阻断时不应被调用。 */
    private static final class CountingPortfolioService extends com.example.dzcom.application.service.portfolio.MockPortfolioApplicationService {
        private int ensureCalls;
        private String lastReportBizId;

        private CountingPortfolioService() {
            super(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        }

        @Override
        public MockPortfolioView ensureAutomationPortfolio(String userBizId, String portfolioName, BigDecimal initialCash) {
            ensureCalls++;
            return portfolio(userBizId);
        }

        @Override
        public MockOrderExecutionView buyFromReport(ExecuteMockPlanFromReportCommand command) {
            lastReportBizId = command.reportBizId();
            MockPortfolioView portfolio = portfolio("user-1");
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
        public MockPortfolioView refreshValuation(String portfolioBizId) {
            return portfolio("user-1");
        }

        private MockPortfolioView portfolio(String userBizId) {
            return MockPortfolioView.builder()
                .bizId("portfolio-1")
                .ownerUserBizId(userBizId)
                .portfolioName("全自动闭环模拟组合")
                .status(1)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
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
        @Override
        public Optional<AiModel> findByBizId(String bizId) {
            return Optional.empty();
        }

        @Override
        public Optional<AiModel> findByCodeAndVersion(String modelCode, String modelVersion) {
            return Optional.empty();
        }

        @Override
        public Optional<AiModel> findActiveByCode(String modelCode) {
            return Optional.empty();
        }

        @Override
        public AiModel save(AiModel model) {
            return model;
        }

        @Override
        public PageResult<AiModel> search(AiModelSearchCriteria criteria) {
            return PageResult.<AiModel>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
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
