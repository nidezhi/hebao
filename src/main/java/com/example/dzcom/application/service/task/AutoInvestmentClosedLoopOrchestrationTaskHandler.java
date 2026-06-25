package com.example.dzcom.application.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.dto.ai.BacktestResultView;
import com.example.dzcom.application.dto.ai.InvestmentFeedbackView;
import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.ai.AiModelApplicationService;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.application.service.portfolio.MockPortfolioApplicationService;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 自动投资闭环总编排任务。 */
@Service
@RequiredArgsConstructor
public class AutoInvestmentClosedLoopOrchestrationTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION";
    private static final String DEFAULT_PORTFOLIO_NAME = "全自动闭环模拟组合";
    private static final String DEFAULT_MODEL_TYPE = "INVESTMENT_ANALYSIS";
    private static final DateTimeFormatter VERSION_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final InvestmentTaskDefinitionStore definitions;
    private final ObjectProvider<InvestmentTaskExecutionService> taskExecution;
    private final ClosedLoopOrchestrationApplicationService closedLoops;
    private final InvestmentAnalysisReportStore reports;
    private final MockPortfolioApplicationService portfolios;
    private final InvestmentClosedLoopApplicationService investmentClosedLoop;
    private final AiModelApplicationService models;
    private final CurrentOperatorProvider currentOperator;
    private final com.example.dzcom.application.common.service.IdGenerator ids;
    private final com.example.dzcom.application.common.service.ClockProvider clock;

    /** 判断当前处理器是否支持自动闭环总编排任务。 */
    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    /**
     * 串联真实数据采集、报告生成、Prompt/模型候选、Mock 交易、回测和反馈。
     *
     * <p>该任务只自动执行 Mock 投资闭环。新 Prompt、新模型和真实交易均只产出候选、
     * 评分或审计记录，不会越过人工确认和灰度开关直接正式启用。</p>
     *
     * @param event 任务事件
     * @return 可写入任务执行审计的摘要
     * @author dz
     * @date 2026-06-25
     */
    @Override
    public String execute(InvestmentTaskEvent event) {
        Map<String, String> parameters = event.parameters() == null ? Map.of() : event.parameters();
        String automationLevel = TaskParameterParser.string(parameters, "automationLevel", "FULL_MOCK");
        String marketScope = TaskParameterParser.marketScope(parameters);
        String mockUserBizId = TaskParameterParser.string(parameters, "mockUserBizId", "");
        ClosedLoopRun run = closedLoops.createRun(
            event.taskCode(),
            event.triggerSource(),
            automationLevel,
            marketScope,
            singleThemeCode(parameters),
            mockUserBizId
        );
        try {
            recordSafetyGuards(run, parameters);
            runConfiguredTasks(run, event, parameters, "dataTaskCodes", "DATA_COLLECTION", "真实数据采集", 20);
            runReportTask(run, event, parameters);
            InvestmentAnalysisReport report = selectExecutableReport(run, parameters);
            run = closedLoops.updateRun(run.toBuilder()
                .reportBizId(report.bizId())
                .qualityScore(report.dataQualityScore())
                .gateResult("PASS")
                .build());
            closedLoops.succeedStep(run, "QUALITY_GATE", "数据质量与报告门禁", 40,
                Map.of("minQualityScore", minQualityScore(parameters)),
                mapOfNullable("reportBizId", report.bizId(), "qualityScore", report.dataQualityScore(),
                    "confidenceLevel", report.confidenceLevel()));
            runPromptCandidateTask(run, event, parameters, report);
            AiModel candidate = saveModelCandidate(run, parameters, report);
            MockPortfolioView portfolio = executeMockTrade(run, parameters, report);
            run = closedLoops.updateRun(run.toBuilder().portfolioBizId(portfolio.bizId()).build());
            BacktestResultView backtest = saveBacktestAndFeedback(run, parameters, report, portfolio);
            run = closedLoops.updateRun(run.toBuilder().backtestBizId(backtest.bizId()).build());
            recordActivationGuards(run, parameters, candidate);
            ClosedLoopRun completed = closedLoops.completeRun(run, "SUCCEEDED", "PASS", null, Map.of(
                "reportBizId", report.bizId(),
                "portfolioBizId", portfolio.bizId(),
                "backtestBizId", backtest.bizId(),
                "modelCandidateBizId", candidate == null ? "" : candidate.bizId(),
                "automationBoundary", "FULL_MOCK_ONLY_FORMAL_ACTIVATION_REQUIRES_REVIEW"
            ));
            return "自动闭环完成: runNo=" + completed.runNo()
                + ", reportBizId=" + report.bizId()
                + ", portfolioBizId=" + portfolio.bizId()
                + ", backtestBizId=" + backtest.bizId();
        } catch (ClosedLoopBlockedException blocked) {
            closedLoops.completeRun(run, "BLOCKED", "BLOCK", blocked.getMessage(), Map.of(
                "blockedReason", blocked.getMessage(),
                "automationBoundary", "STOPPED_BEFORE_MOCK_OR_FORMAL_ACTIVATION"
            ));
            throw blocked;
        } catch (Exception exception) {
            closedLoops.failedStep(run, "UNEXPECTED_FAILURE", "闭环异常兜底", 999,
                exception.getMessage(), Map.of("taskCode", event.taskCode()));
            closedLoops.completeRun(run, "FAILED", "BLOCK", exception.getMessage(), Map.of(
                "failureReason", exception.getMessage()
            ));
            throw exception;
        }
    }

    /** 记录真实交易、自动启用 Prompt/模型等危险开关的保护边界。 */
    private void recordSafetyGuards(ClosedLoopRun run, Map<String, String> parameters) {
        closedLoops.succeedStep(run, "SAFETY_GUARD", "自动化安全边界", 10,
            Map.of(
                "allowRealTrade", TaskParameterParser.bool(parameters, "allowRealTrade", false),
                "allowAutoPromptActivation", TaskParameterParser.bool(parameters, "allowAutoPromptActivation", false),
                "allowAutoModelActivation", TaskParameterParser.bool(parameters, "allowAutoModelActivation", false)
            ),
            Map.of("effectivePolicy", "只允许自动 Mock、候选生成和评分；正式启用与真实交易需要人工确认或灰度开关"));
    }

    /** 按配置同步执行一组子任务。 */
    private void runConfiguredTasks(
        ClosedLoopRun run,
        InvestmentTaskEvent parentEvent,
        Map<String, String> parentParameters,
        String taskCodesKey,
        String stepCode,
        String stepName,
        int stepOrder
    ) {
        List<String> taskCodes = TaskParameterParser.list(parentParameters, taskCodesKey);
        if (taskCodes.isEmpty()) {
            closedLoops.skippedStep(run, stepCode, stepName, stepOrder, "未配置子任务", Map.of("paramKey", taskCodesKey));
            return;
        }
        List<Map<String, Object>> results = taskCodes.stream()
            .map(taskCode -> executeChildTask(parentEvent, taskCode, Map.of()))
            .toList();
        boolean failed = results.stream().anyMatch(result -> !"SUCCEEDED".equals(result.get("status")));
        if (failed) {
            String reason = "子任务执行失败: " + JSON.toJSONString(results);
            closedLoops.blockedStep(run, stepCode, stepName, stepOrder, reason, Map.of("taskCodes", taskCodes));
            throw new ClosedLoopBlockedException(reason);
        }
        closedLoops.succeedStep(run, stepCode, stepName, stepOrder,
            Map.of("taskCodes", taskCodes),
            Map.of("executions", results));
    }

    /** 执行自动报告任务，并把本轮模型、市场和回看窗口参数传给报告处理器。 */
    private void runReportTask(ClosedLoopRun run, InvestmentTaskEvent parentEvent, Map<String, String> parameters) {
        String reportTaskCode = TaskParameterParser.string(parameters, "reportTaskCode", "");
        if (reportTaskCode.isBlank()) {
            String reason = "未配置自动报告任务";
            closedLoops.blockedStep(run, "REPORT_GENERATION", "自动报告生成", 30, reason, Map.of());
            throw new ClosedLoopBlockedException(reason);
        }
        Map<String, String> overrides = new LinkedHashMap<>();
        copyIfPresent(parameters, overrides, "providerCode");
        copyIfPresent(parameters, overrides, "modelCode");
        copyIfPresent(parameters, overrides, "marketScope");
        copyIfPresent(parameters, overrides, "lookbackDays");
        copyIfPresent(parameters, overrides, "themeCodes");
        copyIfPresent(parameters, overrides, "themes");
        copyIfPresent(parameters, overrides, "initialCapital");
        Map<String, Object> result = executeChildTask(parentEvent, reportTaskCode, overrides);
        if (!"SUCCEEDED".equals(result.get("status"))) {
            String reason = "自动报告任务失败: " + result.get("failureReason");
            closedLoops.blockedStep(run, "REPORT_GENERATION", "自动报告生成", 30, reason,
                Map.of("taskCode", reportTaskCode));
            throw new ClosedLoopBlockedException(reason);
        }
        closedLoops.succeedStep(run, "REPORT_GENERATION", "自动报告生成", 30,
            Map.of("taskCode", reportTaskCode, "overrides", overrides),
            result);
    }

    /** 选择最近可进入 Mock 闭环的报告。 */
    private InvestmentAnalysisReport selectExecutableReport(ClosedLoopRun run, Map<String, String> parameters) {
        int maxReports = TaskParameterParser.positiveInt(parameters, "maxReportsForMock", 1);
        BigDecimal minQualityScore = minQualityScore(parameters);
        InvestmentAnalysisReport selected = reports.latest(maxReports).items().stream()
            .filter(report -> isReportStatusSucceeded(report.status()))
            .filter(report -> !"UNUSABLE".equals(report.confidenceLevel()))
            .filter(report -> report.dataQualityScore() != null
                && report.dataQualityScore().compareTo(minQualityScore) >= 0)
            .filter(this::isDataQualityGatePassed)
            .filter(report -> !isDataGapReport(report))
            .findFirst()
            .orElse(null);
        if (selected == null) {
            String reason = "没有找到满足质量门禁的最新投资报告";
            closedLoops.blockedStep(run, "QUALITY_GATE", "数据质量与报告门禁", 40, reason,
                Map.of("maxReportsForMock", maxReports, "minQualityScore", minQualityScore));
            throw new ClosedLoopBlockedException(reason);
        }
        return selected;
    }

    /** 触发 Prompt 候选治理任务。 */
    private void runPromptCandidateTask(
        ClosedLoopRun run,
        InvestmentTaskEvent parentEvent,
        Map<String, String> parameters,
        InvestmentAnalysisReport report
    ) {
        if (!TaskParameterParser.bool(parameters, "allowPromptCandidate", true)) {
            closedLoops.skippedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
                "配置未开启 Prompt 候选生成", Map.of("reportBizId", report.bizId()));
            return;
        }
        String promptTaskCode = TaskParameterParser.string(parameters, "promptTaskCode", "");
        if (promptTaskCode.isBlank()) {
            closedLoops.skippedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
                "未配置 Prompt 治理任务", Map.of("reportBizId", report.bizId()));
            return;
        }
        Map<String, Object> result = executeChildTask(parentEvent, promptTaskCode, Map.of());
        if (!"SUCCEEDED".equals(result.get("status"))) {
            closedLoops.blockedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
                "Prompt 治理任务失败: " + result.get("failureReason"), Map.of("taskCode", promptTaskCode));
            return;
        }
        closedLoops.succeedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
            Map.of("taskCode", promptTaskCode, "reportBizId", report.bizId()),
            result);
    }

    /** 生成 DRAFT 模型候选和评分指标，不自动启用。 */
    private AiModel saveModelCandidate(
        ClosedLoopRun run,
        Map<String, String> parameters,
        InvestmentAnalysisReport report
    ) {
        if (!TaskParameterParser.bool(parameters, "allowModelCandidate", true)) {
            closedLoops.skippedStep(run, "MODEL_CANDIDATE", "模型候选与评分", 60,
                "配置未开启模型候选生成", Map.of("reportBizId", report.bizId()));
            return null;
        }
        String modelCode = TaskParameterParser.string(parameters, "modelCandidateCode",
            TaskParameterParser.string(parameters, "modelCode", report.modelCode()));
        String providerCode = TaskParameterParser.string(parameters, "providerCode", report.providerCode());
        String version = "candidate-" + VERSION_FORMATTER.format(clock.now());
        BigDecimal score = reportScore(report);
        Map<String, Object> modelConfig = new LinkedHashMap<>();
        modelConfig.put("baseModelCode", report.modelCode());
        modelConfig.put("activationPolicy", "MANUAL_REVIEW_OR_GRAY_SWITCH_REQUIRED");
        modelConfig.put("sourceReportBizId", report.bizId());
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("score", score);
        metrics.put("dataQualityScore", report.dataQualityScore());
        metrics.put("confidenceLevel", report.confidenceLevel());
        metrics.put("gatePassed", true);
        AiModel candidate = models.save(
            modelCode,
            version,
            "自动闭环候选模型-" + version,
            DEFAULT_MODEL_TYPE,
            providerCode,
            "closed-loop://" + run.bizId(),
            JSON.toJSONString(modelConfig),
            JSON.toJSONString(metrics),
            "DRAFT"
        );
        closedLoops.succeedStep(run, "MODEL_CANDIDATE", "模型候选与评分", 60,
            Map.of("reportBizId", report.bizId(), "modelCode", modelCode),
            mapOfNullable("modelBizId", candidate.bizId(), "modelVersion", candidate.modelVersion(), "score", score,
                "status", candidate.status()));
        return candidate;
    }

    /** 在配置的模拟用户身份下执行 Mock 买入。 */
    private MockPortfolioView executeMockTrade(
        ClosedLoopRun run,
        Map<String, String> parameters,
        InvestmentAnalysisReport report
    ) {
        if (!TaskParameterParser.bool(parameters, "allowAutoMockTrade", true)) {
            String reason = "配置未开启自动 Mock 交易，闭环停止在报告和候选阶段";
            closedLoops.blockedStep(run, "MOCK_TRADE", "自动Mock交易", 70, reason,
                Map.of("reportBizId", report.bizId()));
            throw new ClosedLoopBlockedException(reason);
        }
        String mockUserBizId = TaskParameterParser.string(parameters, "mockUserBizId", "");
        if (mockUserBizId.isBlank()) {
            String reason = "未配置自动 Mock 用户";
            closedLoops.blockedStep(run, "MOCK_TRADE", "自动Mock交易", 70, reason,
                Map.of("reportBizId", report.bizId()));
            throw new ClosedLoopBlockedException(reason);
        }
        CurrentOperator operator = new CurrentOperator(mockUserBizId, "AUTO_CLOSED_LOOP", Set.of("USER"), Set.of());
        return currentOperator.callAs(operator, () -> {
            BigDecimal initialCash = positiveDecimal(parameters, "initialCash", new BigDecimal("100000"));
            MockPortfolioView portfolio = portfolios.ensureAutomationPortfolio(
                mockUserBizId,
                TaskParameterParser.string(parameters, "mockPortfolioName", DEFAULT_PORTFOLIO_NAME),
                initialCash
            );
            MockOrderExecutionView execution = portfolios.buyFromReport(ExecuteMockPlanFromReportCommand.builder()
                .portfolioBizId(portfolio.bizId())
                .reportBizId(report.bizId())
                .productBizId(TaskParameterParser.string(parameters, "mockProductBizId", null))
                .idempotencyKey("AUTO-CLOSED-LOOP-" + run.bizId() + "-" + report.bizId())
                .build());
            MockPortfolioView refreshed = portfolios.refreshValuation(execution.portfolio().bizId());
            closedLoops.succeedStep(run, "MOCK_TRADE", "自动Mock交易", 70,
                Map.of("reportBizId", report.bizId(), "portfolioBizId", portfolio.bizId()),
                Map.of("orderBizId", execution.order().bizId(), "portfolioBizId", refreshed.bizId(),
                    "status", execution.order().status()));
            return refreshed;
        });
    }

    /** 生成组合回测摘要并写入自动反馈。 */
    private BacktestResultView saveBacktestAndFeedback(
        ClosedLoopRun run,
        Map<String, String> parameters,
        InvestmentAnalysisReport report,
        MockPortfolioView portfolio
    ) {
        CurrentOperator operator = new CurrentOperator(portfolio.ownerUserBizId(), "AUTO_CLOSED_LOOP", Set.of("USER"), Set.of());
        return currentOperator.callAs(operator, () -> {
            BacktestResultView backtest = investmentClosedLoop.generateBacktestFromPortfolio(
                GenerateBacktestFromPortfolioCommand.builder()
                    .portfolioBizId(portfolio.bizId())
                    .strategyCode("AUTO_CLOSED_LOOP_MOCK")
                    .strategyVersion(run.runNo())
                    .benchmarkCode(nullableString(parameters, "benchmarkCode"))
                    .parameters(JSON.toJSONString(Map.of(
                        "reportBizId", report.bizId(),
                        "runBizId", run.bizId(),
                        "source", "AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION"
                    )))
                    .limit(TaskParameterParser.positiveInt(parameters, "valuationPointLimit", 100))
                    .build());
            InvestmentFeedbackView feedback = investmentClosedLoop.saveFeedback(SaveInvestmentFeedbackCommand.builder()
                .targetType("MOCK_PORTFOLIO")
                .targetBizId(portfolio.bizId())
                .reportBizId(report.bizId())
                .promptCode(TaskParameterParser.string(parameters, "promptCode", "investment-plan-from-report"))
                .promptVersion(TaskParameterParser.string(parameters, "promptVersion", "auto-v1"))
                .backtestBizId(backtest.bizId())
                .feedbackAction("WATCH")
                .reasonCode("AUTO_MOCK_EXECUTED")
                .commentText("自动闭环已完成 Mock 交易和回测，等待人工复盘或采纳。")
                .metadata(JSON.toJSONString(mapOfNullable(
                    "runBizId", run.bizId(),
                    "automationLevel", run.automationLevel(),
                    "qualityScore", report.dataQualityScore()
                )))
                .build());
            closedLoops.succeedStep(run, "BACKTEST_FEEDBACK", "回测与反馈沉淀", 80,
                Map.of("portfolioBizId", portfolio.bizId(), "reportBizId", report.bizId()),
                Map.of("backtestBizId", backtest.bizId(), "feedbackBizId", feedback.bizId()));
            return backtest;
        });
    }

    /** 记录正式启用边界，当前版本不自动激活 Prompt 或模型，也不执行真实交易。 */
    private void recordActivationGuards(ClosedLoopRun run, Map<String, String> parameters, AiModel candidate) {
        closedLoops.skippedStep(run, "PROMPT_ACTIVATION_GUARD", "Prompt正式启用闸门", 90,
            "新 Prompt 正式启用需要人工确认或灰度开关，本轮只保留候选与评分",
            Map.of("allowAutoPromptActivation", TaskParameterParser.bool(parameters, "allowAutoPromptActivation", false)));
        closedLoops.skippedStep(run, "MODEL_ACTIVATION_GUARD", "模型正式启用闸门", 91,
            "新模型正式启用需要人工确认或灰度开关，本轮只保留 DRAFT 候选",
            Map.of("allowAutoModelActivation", TaskParameterParser.bool(parameters, "allowAutoModelActivation", false),
                "modelCandidateBizId", candidate == null ? "" : candidate.bizId()));
        closedLoops.skippedStep(run, "REAL_TRADE_GUARD", "真实交易闸门", 92,
            "真实交易需要人工确认或专用灰度开关，自动闭环不会触发真实交易",
            Map.of("allowRealTrade", TaskParameterParser.bool(parameters, "allowRealTrade", false)));
    }

    /** 执行一个配置内子任务。 */
    private Map<String, Object> executeChildTask(
        InvestmentTaskEvent parentEvent,
        String taskCode,
        Map<String, String> overrides
    ) {
        InvestmentTaskDefinition definition = definitions.findByCode(taskCode)
            .orElseThrow(() -> new ClosedLoopBlockedException("投资任务配置不存在: " + taskCode));
        if (TASK_TYPE.equals(definition.taskType())) {
            throw new ClosedLoopBlockedException("闭环总编排任务不能递归调用自身: " + taskCode);
        }
        Map<String, String> parameters = new LinkedHashMap<>(definition.parameters());
        parameters.putAll(overrides);
        ScheduledTaskExecution execution = taskExecution.getObject().executeAndReturn(InvestmentTaskEvent.builder()
            .eventId(ids.newBizId())
            .taskCode(definition.taskCode())
            .taskType(definition.taskType())
            .triggerSource(parentEvent.triggerSource())
            .parameters(parameters)
            .triggeredAt(clock.now())
            .build());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskCode", execution.taskCode());
        result.put("taskType", execution.taskType());
        result.put("eventId", execution.eventId());
        result.put("status", execution.status());
        result.put("resultSummary", execution.resultSummary());
        result.put("failureReason", execution.failureReason());
        return result;
    }

    /** 解析单主题编码，多个主题时由报告任务批量生成，本轮运行记录只保留第一个用于筛选。 */
    private String singleThemeCode(Map<String, String> parameters) {
        List<String> themeCodes = TaskParameterParser.list(parameters, "themeCodes");
        return themeCodes.isEmpty() ? null : themeCodes.get(0);
    }

    /** 拷贝存在的参数。 */
    private void copyIfPresent(Map<String, String> source, Map<String, String> target, String key) {
        String value = source.get(key);
        if (value != null && !value.isBlank()) {
            target.put(key, value);
        }
    }

    /** 读取可为空字符串，避免空文本被当作有效参数向下游传递。 */
    private String nullableString(Map<String, String> parameters, String key) {
        String value = parameters.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 判断报告状态是否成功。 */
    private boolean isReportStatusSucceeded(String status) {
        return "SUCCESS".equals(status) || "SUCCEEDED".equals(status);
    }

    /** 判断报告数据质量门禁是否通过。 */
    private boolean isDataQualityGatePassed(InvestmentAnalysisReport report) {
        if (report.dataQualityGate() == null || report.dataQualityGate().isBlank()) {
            return false;
        }
        return JSON.parseObject(report.dataQualityGate()).getBooleanValue("passed");
    }

    /** 判断报告是否只是数据缺口报告。 */
    private boolean isDataGapReport(InvestmentAnalysisReport report) {
        if (report.investmentPlan() == null || report.investmentPlan().isBlank()) {
            return true;
        }
        JSONObject plan = JSON.parseObject(report.investmentPlan());
        return "DATA_GAP_REPORT".equals(plan.getString("planType"));
    }

    /** 根据报告质量和可信等级计算候选模型评分。 */
    private BigDecimal reportScore(InvestmentAnalysisReport report) {
        BigDecimal quality = report.dataQualityScore() == null ? BigDecimal.ZERO : report.dataQualityScore();
        BigDecimal confidence = switch (report.confidenceLevel() == null ? "" : report.confidenceLevel()) {
            case "HIGH_CONFIDENCE" -> new BigDecimal("0.95");
            case "MEDIUM_CONFIDENCE" -> new BigDecimal("0.70");
            case "LOW_CONFIDENCE" -> new BigDecimal("0.40");
            default -> new BigDecimal("0.15");
        };
        return quality.add(confidence).divide(new BigDecimal("2"), 4, java.math.RoundingMode.HALF_UP);
    }

    /** 读取质量阈值。 */
    private BigDecimal minQualityScore(Map<String, String> parameters) {
        return positiveDecimal(parameters, "minQualityScore", new BigDecimal("0.45"));
    }

    /** 读取正数配置。 */
    private BigDecimal positiveDecimal(Map<String, String> parameters, String key, BigDecimal defaultValue) {
        String value = TaskParameterParser.string(parameters, key, "");
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        BigDecimal parsed = new BigDecimal(value.trim());
        if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(key + " 必须大于 0");
        }
        return parsed;
    }

    /** 构造允许值为空的有序摘要 Map，用于审计 JSON。 */
    private Map<String, Object> mapOfNullable(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put((String) values[index], values[index + 1]);
        }
        return result;
    }

    /** 闭环门禁阻断异常，用于区分业务阻断和系统异常。 */
    private static class ClosedLoopBlockedException extends RuntimeException {
        ClosedLoopBlockedException(String message) {
            super(message);
        }
    }
}
