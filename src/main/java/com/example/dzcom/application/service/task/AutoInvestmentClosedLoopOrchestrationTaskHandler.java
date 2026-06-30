package com.example.dzcom.application.service.task;

import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockBuyCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockRebalanceCommand;
import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.dto.ai.BacktestResultView;
import com.example.dzcom.application.dto.ai.InvestmentFeedbackView;
import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.dto.portfolio.MockRebalanceExecutionView;
import com.example.dzcom.application.dto.portfolio.PositionView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.ai.AiModelApplicationService;
import com.example.dzcom.application.service.ai.InvestmentClosedLoopApplicationService;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.portfolio.MockPortfolioApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 自动投资闭环总编排任务。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoInvestmentClosedLoopOrchestrationTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION";
    private static final String STRUCTURED_DATA_COLLECTION_TASK_TYPE = "AI_STRUCTURED_DATA_COLLECTION";
    private static final String REAL_PRODUCT_TASK_TYPE = "REAL_PRODUCT_UNIVERSE_SYNC";
    private static final String REAL_QUOTE_TASK_TYPE = "REAL_MARKET_QUOTE_SYNC";
    private static final String REAL_NEWS_TASK_TYPE = "REAL_NEWS_SYNC";
    private static final String REAL_QUALITY_TASK_TYPE = "REAL_DATA_QUALITY_SNAPSHOT";
    private static final DateTimeFormatter VERSION_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final InvestmentTaskDefinitionStore definitions;
    private final ObjectProvider<InvestmentTaskExecutionService> taskExecution;
    private final AutoInvestmentClosedLoopConfigService autoInvestmentConfig;
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
     * <p>该任务自动执行真实数据采集、报告生成、Prompt/模型候选、Prompt/模型启用审计、
     * Mock 交易、回测和反馈沉淀。真实交易仍然只记录闸门，不会被自动触发。</p>
     *
     * @param event 任务事件
     * @return 可写入任务执行审计的摘要
     * @author dz
     * @date 2026-06-25
     */
    @Override
    public String execute(InvestmentTaskEvent event) {
        Map<String, String> parameters = new LinkedHashMap<>(event.parameters() == null ? Map.of() : event.parameters());
        String runMode = TaskParameterParser.string(parameters, "runMode", "FULL_PIPELINE");
        String automationLevel = TaskParameterParser.string(
            parameters,
            "automationLevel",
            autoInvestmentConfig.automationLevel()
        );
        String marketScope = TaskParameterParser.marketScope(parameters);
        String mockUserBizId = TaskParameterParser.string(
            parameters,
            "mockUserBizId",
            autoInvestmentConfig.mockUserBizId()
        );
        log.info(
            "自动投资闭环开始: taskCode={}, eventId={}, triggerSource={}, automationLevel={}, marketScope={}, themeCode={}, reportTaskCode={}, dataTaskCodes={}, promptTaskCode={}, modelCode={}, providerCode={}, mockUserConfigured={}",
            event.taskCode(),
            event.eventId(),
            event.triggerSource(),
            automationLevel,
            marketScope,
            singleThemeCode(parameters),
            TaskParameterParser.string(parameters, "reportTaskCode", ""),
            TaskParameterParser.list(parameters, "dataTaskCodes"),
            TaskParameterParser.string(parameters, "promptTaskCode", ""),
            TaskParameterParser.string(parameters, "modelCode", ""),
            TaskParameterParser.string(parameters, "providerCode", ""),
            !mockUserBizId.isBlank()
        );
        ClosedLoopRun run = closedLoops.createRun(
            event.taskCode(),
            event.triggerSource(),
            automationLevel,
            marketScope,
            singleThemeCode(parameters),
            mockUserBizId
        );
        try {
            recordProfileSnapshot(run, parameters, runMode);
            recordSafetyGuards(run, parameters);
            prepareMockPortfolioContext(run, parameters);
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
                "configProfileCode", TaskParameterParser.string(parameters, "configProfileCode", ""),
                "profileType", TaskParameterParser.string(parameters, "profileType", ""),
                "riskLevel", TaskParameterParser.string(parameters, "riskLevel", ""),
                "runMode", runMode,
                "modelCandidateBizId", candidate == null ? "" : candidate.bizId(),
                "automationBoundary", "FULL_MOCK_WITH_PROMPT_MODEL_AUTO_ACTIVATION_REAL_TRADE_DISABLED"
            ));
            log.info(
                "自动投资闭环完成: runNo={}, taskCode={}, reportBizId={}, portfolioBizId={}, backtestBizId={}, modelCandidateBizId={}",
                completed.runNo(),
                event.taskCode(),
                report.bizId(),
                portfolio.bizId(),
                backtest.bizId(),
                candidate == null ? "" : candidate.bizId()
            );
            return "自动闭环完成: runNo=" + completed.runNo()
                + ", reportBizId=" + report.bizId()
                + ", portfolioBizId=" + portfolio.bizId()
                + ", backtestBizId=" + backtest.bizId();
        } catch (ClosedLoopBlockedException blocked) {
            log.warn(
                "自动投资闭环被业务门禁阻断: taskCode={}, eventId={}, runNo={}, reason={}",
                event.taskCode(),
                event.eventId(),
                run.runNo(),
                blocked.getMessage()
            );
            closedLoops.completeRun(run, "BLOCKED", "BLOCK", blocked.getMessage(), Map.of(
                "blockedReason", blocked.getMessage(),
                "automationBoundary", "STOPPED_BEFORE_MOCK_OR_FORMAL_ACTIVATION"
            ));
            throw blocked;
        } catch (Exception exception) {
            log.warn(
                "自动投资闭环异常失败: taskCode={}, eventId={}, runNo={}, exceptionType={}, reason={}",
                event.taskCode(),
                event.eventId(),
                run.runNo(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
            );
            closedLoops.failedStep(run, "UNEXPECTED_FAILURE", "闭环异常兜底", 999,
                exception.getMessage(), Map.of("taskCode", event.taskCode()));
            closedLoops.completeRun(run, "FAILED", "BLOCK", exception.getMessage(), Map.of(
                "failureReason", exception.getMessage()
            ));
            throw exception;
        }
    }

    /**
     * 记录本次闭环使用的高级方案快照，方便前端和审计追溯方案边界。
     *
     * @param run 闭环运行实例
     * @param parameters 本次任务参数
     * @param runMode 运行模式
     * @author dz
     * @date 2026-06-30
     */
    private void recordProfileSnapshot(ClosedLoopRun run, Map<String, String> parameters, String runMode) {
        closedLoops.succeedStep(run, "PROFILE_SNAPSHOT", "闭环方案快照", 5,
            Map.of("configProfileCode", TaskParameterParser.string(parameters, "configProfileCode", "")),
            mapOfNullable(
                "profileName", TaskParameterParser.string(parameters, "profileName", ""),
                "profileType", TaskParameterParser.string(parameters, "profileType", ""),
                "riskLevel", TaskParameterParser.string(parameters, "riskLevel", ""),
                "runMode", runMode,
                "marketScope", TaskParameterParser.marketScope(parameters),
                "dataTaskCodes", TaskParameterParser.list(parameters, "dataTaskCodes"),
                "reportTaskCode", TaskParameterParser.string(parameters, "reportTaskCode", ""),
                "promptTaskCode", TaskParameterParser.string(parameters, "promptTaskCode", ""),
                "minQualityScore", minQualityScore(parameters),
                "maxReportsForMock", TaskParameterParser.positiveInt(parameters, "maxReportsForMock", 20),
                "allowAutoMockTrade", TaskParameterParser.bool(parameters, "allowAutoMockTrade", true),
                "allowAutoPromptActivation", TaskParameterParser.bool(parameters, "allowAutoPromptActivation", true),
                "allowAutoModelActivation", TaskParameterParser.bool(parameters, "allowAutoModelActivation", true),
                "allowRealTrade", TaskParameterParser.bool(parameters, "allowRealTrade", false),
                "maxSingleTradeAmount", TaskParameterParser.string(parameters, "maxSingleTradeAmount", ""),
                "strategyNote", TaskParameterParser.string(parameters, "strategyNote", "")
            ));
    }

    /** 记录真实交易、自动启用 Prompt/模型等危险开关的保护边界。 */
    private void recordSafetyGuards(ClosedLoopRun run, Map<String, String> parameters) {
        closedLoops.succeedStep(run, "SAFETY_GUARD", "自动化权限确认", 10,
            Map.of(
                "allowRealTrade", TaskParameterParser.bool(parameters, "allowRealTrade", false),
                "allowAutoPromptActivation", TaskParameterParser.bool(parameters, "allowAutoPromptActivation", true),
                "allowAutoModelActivation", TaskParameterParser.bool(parameters, "allowAutoModelActivation", true),
                "configProfileCode", TaskParameterParser.string(parameters, "configProfileCode", ""),
                "blocking", false,
                "displaySeverity", "INFO",
                "userFacing", false
            ),
            Map.of(
                "policyCode", "FULL_MOCK_WITH_PROMPT_MODEL_AUTO_ACTIVATION",
                "configProfileCode", TaskParameterParser.string(parameters, "configProfileCode", ""),
                "configProfileSnapshot", TaskParameterParser.string(parameters, "configProfileSnapshot", ""),
                "blocking", false,
                "displaySeverity", "INFO",
                "userFacing", false,
                "summary", "自动闭环允许真实数据采集、报告生成、候选评分、Prompt/模型自动启用和Mock交易；真实交易仍需人工确认。"
            ));
    }

    /**
     * 在报告生成前准备 Mock 组合上下文，让大模型基于真实资金池现金、估值和持仓输出方案。
     *
     * @param run 闭环运行实例
     * @param parameters 本次任务参数，会写入 portfolioContext 和 mockPortfolioBizId
     * @author dz
     * @date 2026-06-30
     */
    private void prepareMockPortfolioContext(ClosedLoopRun run, Map<String, String> parameters) {
        if (!TaskParameterParser.bool(parameters, "allowAutoMockTrade", true)) {
            return;
        }
        if (TaskParameterParser.bool(parameters, "skipReportTask", false)) {
            return;
        }
        String mockUserBizId = TaskParameterParser.string(parameters, "mockUserBizId", autoInvestmentConfig.mockUserBizId());
        String mockPortfolioBizId = TaskParameterParser.string(parameters, "mockPortfolioBizId", autoInvestmentConfig.mockPortfolioBizId());
        MockPortfolioView configuredPortfolio = portfolios.configuredAutomationPortfolio(mockPortfolioBizId).orElse(null);
        String effectiveMockUserBizId = configuredPortfolio == null || configuredPortfolio.ownerUserBizId() == null
            || configuredPortfolio.ownerUserBizId().isBlank()
            ? mockUserBizId
            : configuredPortfolio.ownerUserBizId();
        CurrentOperator operator = new CurrentOperator(effectiveMockUserBizId, "AUTO_CLOSED_LOOP", Set.of("USER"), Set.of());
        MockPortfolioView portfolio = currentOperator.callAs(operator, () -> configuredPortfolio == null
            ? portfolios.ensureAutomationPortfolio(
                effectiveMockUserBizId,
                TaskParameterParser.string(parameters, "mockPortfolioName", autoInvestmentConfig.mockPortfolioName()),
                positiveDecimal(parameters, "initialCash", autoInvestmentConfig.initialCash())
            )
            : configuredPortfolio);
        parameters.put("mockPortfolioBizId", portfolio.bizId());
        parameters.put("portfolioContext", Jsons.toJson(portfolioContext(run, parameters, portfolio)));
        closedLoops.succeedStep(run, "MOCK_PORTFOLIO_CONTEXT", "Mock组合上下文", 15,
            Map.of("portfolioBizId", portfolio.bizId(), "mockUserBizId", effectiveMockUserBizId),
            portfolioContext(run, parameters, portfolio));
    }

    /** 构造传给模型和执行器的 Mock 组合上下文。 */
    private Map<String, Object> portfolioContext(
        ClosedLoopRun run,
        Map<String, String> parameters,
        MockPortfolioView portfolio
    ) {
        return mapOfNullable(
            "runBizId", run.bizId(),
            "runNo", run.runNo(),
            "portfolioBizId", portfolio.bizId(),
            "portfolioName", portfolio.portfolioName(),
            "ownerUserBizId", portfolio.ownerUserBizId(),
            "baseCurrency", portfolio.baseCurrency(),
            "totalAsset", portfolio.latestValuation() == null ? BigDecimal.ZERO : portfolio.latestValuation().totalAsset(),
            "cashBalance", portfolio.latestValuation() == null ? BigDecimal.ZERO : portfolio.latestValuation().cashBalance(),
            "positionValue", portfolio.latestValuation() == null ? BigDecimal.ZERO : portfolio.latestValuation().positionValue(),
            "maxSingleTradeAmount", TaskParameterParser.string(parameters, "maxSingleTradeAmount", ""),
            "positions", portfolio.positions() == null ? List.of() : portfolio.positions().stream().map(this::positionContext).toList()
        );
    }

    /** 构造单个持仓上下文。 */
    private Map<String, Object> positionContext(PositionView position) {
        return mapOfNullable(
            "productBizId", position.productBizId(),
            "positionSide", position.positionSide(),
            "quantity", position.quantity(),
            "availableQuantity", position.availableQuantity(),
            "averageCost", position.averageCost(),
            "costAmount", position.costAmount(),
            "realizedProfit", position.realizedProfit()
        );
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
            log.info("自动投资闭环子任务组跳过: runNo={}, stepCode={}, taskCodesKey={}, reason=未配置子任务",
                run.runNo(), stepCode, taskCodesKey);
            closedLoops.skippedStep(run, stepCode, stepName, stepOrder, "未配置子任务", Map.of("paramKey", taskCodesKey));
            return;
        }
        log.info("自动投资闭环子任务组开始: runNo={}, stepCode={}, taskCodes={}", run.runNo(), stepCode, taskCodes);
        List<Map<String, Object>> results = taskCodes.stream()
            .map(taskCode -> executeChildTask(parentEvent, taskCode, Map.of()))
            .toList();
        boolean failed = results.stream().anyMatch(result -> !"SUCCEEDED".equals(result.get("status")));
        if (failed) {
            String reason = "子任务执行失败: " + Jsons.toJson(results);
            log.warn("自动投资闭环子任务组失败: runNo={}, stepCode={}, reason={}", run.runNo(), stepCode, reason);
            closedLoops.blockedStep(run, stepCode, stepName, stepOrder, reason, Map.of("taskCodes", taskCodes));
            throw new ClosedLoopBlockedException(reason);
        }
        enforceRealCoreData(run, parentParameters, stepCode, stepName, stepOrder, results);
        log.info("自动投资闭环子任务组完成: runNo={}, stepCode={}, results={}", run.runNo(), stepCode, Jsons.toJson(results));
        closedLoops.succeedStep(run, stepCode, stepName, stepOrder,
            Map.of("taskCodes", taskCodes),
            Map.of("executions", results));
    }

    /** 主闭环报告前必须有真实核心数据，避免空数据继续生成低质量报告。 */
    private void enforceRealCoreData(
        ClosedLoopRun run,
        Map<String, String> parentParameters,
        String stepCode,
        String stepName,
        int stepOrder,
        List<Map<String, Object>> results
    ) {
        if (!"DATA_COLLECTION".equals(stepCode)
            || !TaskParameterParser.bool(parentParameters, "requireStructuredCoreData", true)) {
            return;
        }
        List<RealDataSummary> realSummaries = results.stream()
            .map(this::realDataSummary)
            .toList();
        int productCount = realDataCount(realSummaries, RealDataSummary::productCount);
        int quoteCount = realDataCount(realSummaries, RealDataSummary::quoteCount);
        int newsCount = realDataCount(realSummaries, RealDataSummary::newsCount);
        BigDecimal qualityScore = realSummaries.stream()
            .map(RealDataSummary::qualityScore)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::max);
        int minProductCount = TaskParameterParser.positiveInt(parentParameters, "minStructuredProductCount", 1);
        int minNewsCount = TaskParameterParser.positiveInt(parentParameters, "minStructuredNewsCount", 1);
        int minQuoteCount = TaskParameterParser.positiveInt(parentParameters, "minStructuredQuoteCount", 1);
        BigDecimal minRealDataQualityScore = new BigDecimal(TaskParameterParser.string(
            parentParameters, "minRealDataQualityScore", "0.60"));
        boolean hasRealDataTasks = realSummaries.stream().anyMatch(RealDataSummary::realTask);
        if (hasRealDataTasks) {
            if (productCount < minProductCount || newsCount < minNewsCount || quoteCount < minQuoteCount
                || qualityScore.compareTo(minRealDataQualityScore) < 0) {
                String reason = "真实核心数据不足: productCount=" + productCount
                    + ", newsCount=" + newsCount
                    + ", quoteCount=" + quoteCount
                    + ", qualityScore=" + qualityScore
                    + ", minProductCount=" + minProductCount
                    + ", minStructuredNewsCount=" + minNewsCount
                    + ", minStructuredQuoteCount=" + minQuoteCount
                    + ", minRealDataQualityScore=" + minRealDataQualityScore;
                log.warn("自动投资闭环真实核心数据门禁阻断: runNo={}, reason={}", run.runNo(), reason);
                closedLoops.blockedStep(run, stepCode, stepName, stepOrder, reason, Map.of(
                    "productCount", productCount,
                    "newsCount", newsCount,
                    "quoteCount", quoteCount,
                    "qualityScore", qualityScore,
                    "summaries", realSummaries.stream().map(RealDataSummary::asMap).toList()
                ));
                throw new ClosedLoopBlockedException(reason);
            }
            return;
        }
        List<StructuredDataSummary> summaries = results.stream()
            .filter(result -> STRUCTURED_DATA_COLLECTION_TASK_TYPE.equals(result.get("taskType")))
            .map(this::structuredSummary)
            .toList();
        if (summaries.isEmpty()) {
            String reason = "主闭环未执行AI结构化核心数据采集任务，不能继续生成投资报告";
            log.warn("自动投资闭环结构化核心数据门禁阻断: runNo={}, reason={}, results={}",
                run.runNo(), reason, Jsons.toJson(results));
            closedLoops.blockedStep(run, stepCode, stepName, stepOrder, reason, Map.of(
                "requiredTaskType", STRUCTURED_DATA_COLLECTION_TASK_TYPE,
                "results", results
            ));
            throw new ClosedLoopBlockedException(reason);
        }
        int aiNewsCount = summaries.stream().mapToInt(StructuredDataSummary::newsCount).sum();
        int aiProductCount = summaries.stream().mapToInt(StructuredDataSummary::productCount).sum();
        int aiQuoteCount = summaries.stream().mapToInt(StructuredDataSummary::quoteCount).sum();
        int minAiNewsCount = TaskParameterParser.positiveInt(parentParameters, "minStructuredNewsCount", 1);
        int minAiQuoteCount = TaskParameterParser.positiveInt(parentParameters, "minStructuredQuoteCount", 1);
        if (aiNewsCount < minAiNewsCount || aiQuoteCount < minAiQuoteCount) {
            String reason = "AI结构化核心数据不足: newsCount=" + aiNewsCount
                + ", productCount=" + aiProductCount
                + ", quoteCount=" + aiQuoteCount
                + ", minStructuredNewsCount=" + minAiNewsCount
                + ", minStructuredQuoteCount=" + minAiQuoteCount;
            log.warn("自动投资闭环结构化核心数据门禁阻断: runNo={}, reason={}", run.runNo(), reason);
            closedLoops.blockedStep(run, stepCode, stepName, stepOrder, reason, Map.of(
                "requiredTaskType", STRUCTURED_DATA_COLLECTION_TASK_TYPE,
                "newsCount", aiNewsCount,
                "productCount", aiProductCount,
                "quoteCount", aiQuoteCount,
                "summaries", summaries.stream().map(StructuredDataSummary::asMap).toList()
            ));
            throw new ClosedLoopBlockedException(reason);
        }
    }

    /** 从真实数据子任务摘要提取产品、行情、资讯和质量计数。 */
    private RealDataSummary realDataSummary(Map<String, Object> result) {
        String taskType = String.valueOf(result.getOrDefault("taskType", ""));
        String summary = result.get("resultSummary") == null ? "" : String.valueOf(result.get("resultSummary"));
        boolean realTask = Set.of(REAL_PRODUCT_TASK_TYPE, REAL_QUOTE_TASK_TYPE, REAL_NEWS_TASK_TYPE, REAL_QUALITY_TASK_TYPE)
            .contains(taskType);
        int productCount = REAL_PRODUCT_TASK_TYPE.equals(taskType)
            ? extractInt(summary, "saved")
            : REAL_QUALITY_TASK_TYPE.equals(taskType) ? extractInt(summary, "products") : 0;
        int quoteCount = REAL_QUOTE_TASK_TYPE.equals(taskType)
            ? extractInt(summary, "saved")
            : REAL_QUALITY_TASK_TYPE.equals(taskType) ? extractInt(summary, "quoteReady") : 0;
        int newsCount = REAL_NEWS_TASK_TYPE.equals(taskType)
            ? extractInt(summary, "saved")
            : REAL_QUALITY_TASK_TYPE.equals(taskType) ? extractInt(summary, "recentNews") : 0;
        BigDecimal qualityScore = REAL_QUALITY_TASK_TYPE.equals(taskType)
            ? extractDecimal(summary, "quality")
            : null;
        return new RealDataSummary(taskType, productCount, quoteCount, newsCount, qualityScore, realTask, summary);
    }

    /** 优先使用真实采集任务计数；只有低成本验证仅执行质量快照时，才使用质量快照内的汇总计数。 */
    private int realDataCount(List<RealDataSummary> summaries, java.util.function.ToIntFunction<RealDataSummary> counter) {
        int directCount = summaries.stream()
            .filter(summary -> !REAL_QUALITY_TASK_TYPE.equals(summary.taskType()))
            .mapToInt(counter)
            .sum();
        if (directCount > 0) {
            return directCount;
        }
        return summaries.stream()
            .filter(summary -> REAL_QUALITY_TASK_TYPE.equals(summary.taskType()))
            .mapToInt(counter)
            .sum();
    }

    /** 从“key=value”摘要中提取整数。 */
    private int extractInt(String summary, String key) {
        String value = extractValue(summary, key);
        if (value.isBlank()) {
            return 0;
        }
        try {
            return new BigDecimal(value).intValue();
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /** 从“key=value”摘要中提取小数。 */
    private BigDecimal extractDecimal(String summary, String key) {
        String value = extractValue(summary, key);
        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    /** 解析摘要中的 key=value 值。 */
    private String extractValue(String summary, String key) {
        if (summary == null || summary.isBlank()) {
            return "";
        }
        String marker = key + "=";
        int start = summary.indexOf(marker);
        if (start < 0) {
            return "";
        }
        int valueStart = start + marker.length();
        int end = summary.indexOf(",", valueStart);
        if (end < 0) {
            end = summary.length();
        }
        return summary.substring(valueStart, end).trim();
    }

    /** 从子任务摘要提取结构化采集落库计数。 */
    private StructuredDataSummary structuredSummary(Map<String, Object> result) {
        String summary = result.get("resultSummary") == null ? "" : String.valueOf(result.get("resultSummary"));
        if (summary.isBlank()) {
            return new StructuredDataSummary(0, 0, 0);
        }
        var root = Jsons.readObjectOrEmpty(summary);
        return new StructuredDataSummary(
            Jsons.integer(root, "savedNewsCount", 0),
            Jsons.integer(root, "upsertedProductCount", 0),
            Jsons.integer(root, "savedQuoteCount", 0)
        );
    }

    /** 执行自动报告任务，并把本轮模型、市场和回看窗口参数传给报告处理器。 */
    private void runReportTask(ClosedLoopRun run, InvestmentTaskEvent parentEvent, Map<String, String> parameters) {
        if (TaskParameterParser.bool(parameters, "skipReportTask", false)) {
            String reason = "已配置跳过自动报告任务，复用候选窗口内最新合格报告";
            log.info("自动投资闭环报告任务跳过: runNo={}, reason={}", run.runNo(), reason);
            closedLoops.skippedStep(run, "REPORT_GENERATION", "自动报告生成", 30, reason, Map.of(
                "skipReportTask", true,
                "maxReportsForMock", TaskParameterParser.positiveInt(parameters, "maxReportsForMock", 20)
            ));
            return;
        }
        String reportTaskCode = TaskParameterParser.string(parameters, "reportTaskCode", "");
        if (reportTaskCode.isBlank()) {
            String reason = "未配置自动报告任务";
            log.warn("自动投资闭环报告任务阻断: runNo={}, reason={}", run.runNo(), reason);
            closedLoops.blockedStep(run, "REPORT_GENERATION", "自动报告生成", 30, reason, Map.of());
            throw new ClosedLoopBlockedException(reason);
        }
        Map<String, String> overrides = new LinkedHashMap<>();
        copyIfPresent(parameters, overrides, "providerCode");
        copyIfPresent(parameters, overrides, "modelCode");
        copyIfPresent(parameters, overrides, "marketScope");
        copyIfPresent(parameters, overrides, "lookbackDays");
        copyIfKeyPresent(parameters, overrides, "themeCodes");
        copyIfPresent(parameters, overrides, "themes");
        copyIfPresent(parameters, overrides, "initialCapital");
        copyIfPresent(parameters, overrides, "portfolioContext");
        copyIfPresent(parameters, overrides, "maxThemeReports");
        log.info(
            "自动投资闭环报告任务开始: runNo={}, reportTaskCode={}, overrides={}",
            run.runNo(),
            reportTaskCode,
            overrides
        );
        Map<String, Object> result = executeChildTask(parentEvent, reportTaskCode, overrides);
        if (!"SUCCEEDED".equals(result.get("status"))) {
            String reason = "自动报告任务失败: " + result.get("failureReason");
            log.warn(
                "自动投资闭环报告任务失败: runNo={}, reportTaskCode={}, status={}, failureReason={}",
                run.runNo(),
                reportTaskCode,
                result.get("status"),
                result.get("failureReason")
            );
            closedLoops.blockedStep(run, "REPORT_GENERATION", "自动报告生成", 30, reason,
                Map.of("taskCode", reportTaskCode));
            throw new ClosedLoopBlockedException(reason);
        }
        log.info("自动投资闭环报告任务完成: runNo={}, reportTaskCode={}, result={}", run.runNo(), reportTaskCode, result);
        closedLoops.succeedStep(run, "REPORT_GENERATION", "自动报告生成", 30,
            Map.of("taskCode", reportTaskCode, "overrides", overrides),
            result);
    }

    /** 选择最近可进入 Mock 闭环的报告。 */
    private InvestmentAnalysisReport selectExecutableReport(ClosedLoopRun run, Map<String, String> parameters) {
        int maxReports = TaskParameterParser.positiveInt(parameters, "maxReportsForMock", 20);
        BigDecimal minQualityScore = minQualityScore(parameters);
        List<ReportGateCheck> checks = reports.latest(maxReports).items().stream()
            .map(report -> evaluateReportGate(report, minQualityScore))
            .toList();
        log.info(
            "自动投资闭环报告质量门禁检查: runNo={}, maxReports={}, minQualityScore={}, evaluatedReportCount={}, evaluatedReports={}",
            run.runNo(),
            maxReports,
            minQualityScore,
            checks.size(),
            checks.stream().map(ReportGateCheck::summary).toList()
        );
        InvestmentAnalysisReport selected = checks.stream()
            .filter(ReportGateCheck::passed)
            .map(ReportGateCheck::report)
            .findFirst()
            .orElse(null);
        if (selected == null) {
            String reason = "没有找到满足质量门禁的最新投资报告";
            log.warn(
                "自动投资闭环报告质量门禁阻断: runNo={}, minQualityScore={}, evaluatedReportCount={}, evaluatedReports={}",
                run.runNo(),
                minQualityScore,
                checks.size(),
                checks.stream().map(ReportGateCheck::summary).toList()
            );
            closedLoops.blockedStep(run, "QUALITY_GATE", "数据质量与报告门禁", 40, reason,
                Map.of(
                    "maxReportsForMock", maxReports,
                    "minQualityScore", minQualityScore,
                    "evaluatedReportCount", checks.size(),
                    "evaluatedReports", checks.stream().map(ReportGateCheck::summary).toList()
                ));
            throw new ClosedLoopBlockedException(reason);
        }
        log.info(
            "自动投资闭环报告质量门禁通过: runNo={}, reportBizId={}, status={}, confidenceLevel={}, dataQualityScore={}, themeCode={}",
            run.runNo(),
            selected.bizId(),
            selected.status(),
            selected.confidenceLevel(),
            selected.dataQualityScore(),
            selected.themeCode()
        );
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
            log.info("自动投资闭环Prompt候选跳过: runNo={}, reportBizId={}, reason=配置未开启", run.runNo(), report.bizId());
            closedLoops.skippedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
                "配置未开启 Prompt 候选生成", Map.of("reportBizId", report.bizId()));
            return;
        }
        String promptTaskCode = TaskParameterParser.string(parameters, "promptTaskCode", "");
        if (promptTaskCode.isBlank()) {
            log.info("自动投资闭环Prompt候选跳过: runNo={}, reportBizId={}, reason=未配置任务", run.runNo(), report.bizId());
            closedLoops.skippedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
                "未配置 Prompt 治理任务", Map.of("reportBizId", report.bizId()));
            return;
        }
        String promptCode = TaskParameterParser.string(parameters, "promptCode", autoInvestmentConfig.promptCode());
        String promptVersion = TaskParameterParser.string(parameters, "promptVersion", autoInvestmentConfig.promptVersion());
        String promptScenario = TaskParameterParser.string(parameters, "promptScenario", autoInvestmentConfig.promptScenario());
        log.info("自动投资闭环Prompt候选任务开始: runNo={}, promptTaskCode={}, reportBizId={}",
            run.runNo(), promptTaskCode, report.bizId());
        Map<String, Object> result = executeChildTask(parentEvent, promptTaskCode, mapOfString(
            "promptCode", promptCode,
            "promptVersion", promptVersion,
            "scenario", promptScenario,
            "reportBizId", report.bizId()
        ));
        if (!"SUCCEEDED".equals(result.get("status"))) {
            log.warn("自动投资闭环Prompt候选任务失败: runNo={}, promptTaskCode={}, result={}",
                run.runNo(), promptTaskCode, result);
            closedLoops.blockedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
                "Prompt 治理任务失败: " + result.get("failureReason"), Map.of("taskCode", promptTaskCode));
            return;
        }
        Map<String, Object> promptSummary = readObjectMapOrEmptySafe(result.get("resultSummary"));
        log.info("自动投资闭环Prompt候选任务完成: runNo={}, promptTaskCode={}, result={}",
            run.runNo(), promptTaskCode, result);
        closedLoops.succeedStep(run, "PROMPT_CANDIDATE", "Prompt候选与评分", 50,
            Map.of("taskCode", promptTaskCode, "reportBizId", report.bizId(), "promptCode", promptCode,
                "promptVersion", promptVersion, "scenario", promptScenario),
            mapOfNullable(
                "taskCode", result.get("taskCode"),
                "taskType", result.get("taskType"),
                "eventId", result.get("eventId"),
                "status", result.get("status"),
                "resultSummary", result.get("resultSummary"),
                "failureReason", result.get("failureReason"),
                "reportBizId", report.bizId(),
                "promptBizId", promptSummary.get("promptBizId"),
                "promptCode", promptCode,
                "promptVersion", promptVersion,
                "scenario", promptScenario,
                "evaluationBizId", promptSummary.get("evaluationBizId"),
                "readyForModel", promptSummary.get("readyForModel"),
                "missingVariables", promptSummary.get("missingVariables"),
                "renderedPromptPreview", promptSummary.get("renderedPromptPreview"),
                "summary", promptSummary.get("summary")
            ));
    }

    private Map<String, Object> readObjectMapOrEmptySafe(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return Map.of();
        }
        try {
            return Jsons.readObjectMapOrEmpty(String.valueOf(value));
        } catch (IllegalArgumentException exception) {
            return Map.of();
        }
    }

    /** 生成模型候选和评分指标，正式启用由后续自动化闸门决定。 */
    private AiModel saveModelCandidate(
        ClosedLoopRun run,
        Map<String, String> parameters,
        InvestmentAnalysisReport report
    ) {
        if (!TaskParameterParser.bool(parameters, "allowModelCandidate", true)) {
            log.info("自动投资闭环模型候选跳过: runNo={}, reportBizId={}, reason=配置未开启", run.runNo(), report.bizId());
            closedLoops.skippedStep(run, "MODEL_CANDIDATE", "模型候选与评分", 60,
                "配置未开启模型候选生成", Map.of("reportBizId", report.bizId()));
            return null;
        }
        String modelCode = TaskParameterParser.string(parameters, "modelCandidateCode",
            TaskParameterParser.string(parameters, "modelCode", report.modelCode()));
        String providerCode = TaskParameterParser.string(parameters, "providerCode", report.providerCode());
        String version = "candidate-" + VERSION_FORMATTER.format(clock.now());
        BigDecimal score = reportScore(report);
        AiModel activeBaseModel = models.activeByCode(modelCode);
        Map<String, Object> modelConfig = new LinkedHashMap<>(Jsons.readObjectMapOrEmpty(activeBaseModel.modelConfig()));
        modelConfig.put("baseModelBizId", activeBaseModel.bizId());
        modelConfig.put("baseModelCode", activeBaseModel.modelCode());
        modelConfig.put("baseModelVersion", activeBaseModel.modelVersion());
        modelConfig.put("activationPolicy", "AUTO_ACTIVATION_ALLOWED_WITH_REAL_TRADE_DISABLED");
        modelConfig.put("sourceReportBizId", report.bizId());
        modelConfig.put("generatedBy", TASK_TYPE);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("score", score);
        metrics.put("dataQualityScore", report.dataQualityScore());
        metrics.put("confidenceLevel", report.confidenceLevel());
        metrics.put("gatePassed", true);
        AiModel candidate = models.save(
            modelCode,
            version,
            "自动闭环候选模型-" + version,
            autoInvestmentConfig.modelType(),
            providerCode,
            activeBaseModel.artifactUri(),
            Jsons.toJson(modelConfig),
            Jsons.toJson(metrics),
            "DRAFT"
        );
        log.info(
            "自动投资闭环模型候选已保存: runNo={}, reportBizId={}, modelBizId={}, modelCode={}, modelVersion={}, providerCode={}, score={}",
            run.runNo(),
            report.bizId(),
            candidate.bizId(),
            candidate.modelCode(),
            candidate.modelVersion(),
            providerCode,
            score
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
            log.warn("自动投资闭环Mock交易阻断: runNo={}, reportBizId={}, reason={}", run.runNo(), report.bizId(), reason);
            closedLoops.blockedStep(run, "MOCK_TRADE", "自动Mock交易", 70, reason,
                Map.of("reportBizId", report.bizId()));
            throw new ClosedLoopBlockedException(reason);
        }
        String mockUserBizId = TaskParameterParser.string(
            parameters,
            "mockUserBizId",
            autoInvestmentConfig.mockUserBizId()
        );
        String mockPortfolioBizId = TaskParameterParser.string(
            parameters,
            "mockPortfolioBizId",
            autoInvestmentConfig.mockPortfolioBizId()
        );
        MockPortfolioView configuredPortfolio = portfolios.configuredAutomationPortfolio(mockPortfolioBizId).orElse(null);
        String effectiveMockUserBizId = configuredPortfolio == null || configuredPortfolio.ownerUserBizId() == null
            || configuredPortfolio.ownerUserBizId().isBlank()
            ? mockUserBizId
            : configuredPortfolio.ownerUserBizId();
        CurrentOperator operator = new CurrentOperator(effectiveMockUserBizId, "AUTO_CLOSED_LOOP", Set.of("USER"), Set.of());
        return currentOperator.callAs(operator, () -> {
            BigDecimal initialCash = positiveDecimal(parameters, "initialCash", autoInvestmentConfig.initialCash());
            String portfolioName = TaskParameterParser.string(
                parameters,
                "mockPortfolioName",
                autoInvestmentConfig.mockPortfolioName()
            );
            log.info(
                "自动投资闭环Mock交易开始: runNo={}, reportBizId={}, mockUserBizId={}, mockPortfolioBizId={}, initialCash={}, mockProductBizId={}",
                run.runNo(),
                report.bizId(),
                effectiveMockUserBizId,
                mockPortfolioBizId,
                initialCash,
                TaskParameterParser.string(parameters, "mockProductBizId", null)
            );
            MockPortfolioView portfolio = configuredPortfolio == null
                ? portfolios.ensureAutomationPortfolio(
                    effectiveMockUserBizId,
                    portfolioName,
                    initialCash
                )
                : configuredPortfolio;
            String actionType = actionType(report);
            List<ExecuteMockRebalanceCommand.TargetWeight> targets = targetWeights(report);
            MockExecutionResult execution = executeMockPlan(run, parameters, report, portfolio);
            MockPortfolioView refreshed = portfolios.refreshValuation(execution.portfolio().bizId());
            closedLoops.succeedStep(run, "MOCK_TRADE", "自动Mock交易", 70,
                mapOfNullable("reportBizId", report.bizId(), "portfolioBizId", portfolio.bizId(),
                    "mockUserBizId", effectiveMockUserBizId,
                    "actionType", actionType,
                    "targetWeightCount", targets.size(),
                    "maxSingleTradeAmount", TaskParameterParser.string(parameters, "maxSingleTradeAmount", ""),
                    "investmentPlan", report.investmentPlan()),
                mapOfNullable(
                    "actionType", actionType,
                    "executionMode", execution.mode(),
                    "orderBizId", execution.orderBizId(),
                    "generatedOrderCount", execution.orderCount(),
                    "targetWeightCount", execution.targetWeightCount(),
                    "reason", execution.reason(),
                    "portfolioBizId", refreshed.bizId(),
                    "configuredPortfolioBizId", mockPortfolioBizId,
                    "portfolioName", refreshed.portfolioName(),
                    "mockUserBizId", effectiveMockUserBizId,
                    "aiPoolInitialCash", initialCash,
                    "latestTotalAsset", refreshed.latestValuation() == null ? BigDecimal.ZERO : refreshed.latestValuation().totalAsset(),
                    "latestCashBalance", refreshed.latestValuation() == null ? BigDecimal.ZERO : refreshed.latestValuation().cashBalance(),
                    "status", execution.status()
                ));
            log.info(
                "自动投资闭环Mock交易完成: runNo={}, reportBizId={}, portfolioBizId={}, executionMode={}, orderBizId={}, orderStatus={}",
                run.runNo(),
                report.bizId(),
                refreshed.bizId(),
                execution.mode(),
                execution.orderBizId(),
                execution.status()
            );
            return refreshed;
        });
    }

    /**
     * 基于报告方案和组合上下文执行 Mock 计划；优先调仓，无法调仓时才降级为单笔买入。
     */
    private MockExecutionResult executeMockPlan(
        ClosedLoopRun run,
        Map<String, String> parameters,
        InvestmentAnalysisReport report,
        MockPortfolioView portfolio
    ) {
        try {
            List<ExecuteMockRebalanceCommand.TargetWeight> targets = targetWeights(report);
            String actionType = actionType(report);
            if (!targets.isEmpty() && !"BUY".equals(actionType)) {
                MockRebalanceExecutionView rebalance = portfolios.rebalance(ExecuteMockRebalanceCommand.builder()
                    .portfolioBizId(portfolio.bizId())
                    .targets(targets)
                    .minTradeAmount(positiveDecimal(parameters, "minTradeAmount", new BigDecimal("100")))
                    .idempotencyKey("AUTO-CLOSED-LOOP-" + run.bizId() + "-" + report.bizId())
                    .build());
                if (rebalance.executions() == null || rebalance.executions().isEmpty()) {
                    String reason = "报告方案与当前组合接近，无需生成新的 Mock 订单";
                    closedLoops.skippedStep(run, "MOCK_TRADE_NOOP", "Mock交易无动作", 71,
                        reason, Map.of("portfolioBizId", portfolio.bizId(), "actionType", actionType, "targetWeights", targets.size()));
                    return new MockExecutionResult("HOLD", null, 0, "NO_ORDER", rebalance.portfolio(), reason, targets.size());
                }
                MockOrderExecutionView first = rebalance.executions().get(0);
                return new MockExecutionResult("REBALANCE", first.order().bizId(), rebalance.executions().size(),
                    first.order().status(), rebalance.portfolio(), "已按报告目标权重执行模拟调仓", targets.size());
            }
            if (isNoTradeAction(actionType)) {
                String reason = "报告方案建议" + actionType + "，本轮不生成新的 Mock 订单";
                closedLoops.skippedStep(run, "MOCK_TRADE_NOOP", "Mock交易无动作", 71,
                    reason, Map.of("portfolioBizId", portfolio.bizId(), "actionType", actionType));
                return new MockExecutionResult(actionType, null, 0, "NO_ORDER", portfolio, reason, targets.size());
            }
            MockOrderExecutionView buy = portfolios.buyFromReport(ExecuteMockPlanFromReportCommand.builder()
                .portfolioBizId(portfolio.bizId())
                .reportBizId(report.bizId())
                .productBizId(TaskParameterParser.string(parameters, "mockProductBizId", null))
                .maxTradeAmount(positiveDecimal(parameters, "maxSingleTradeAmount", new BigDecimal("10000")))
                .idempotencyKey("AUTO-CLOSED-LOOP-" + run.bizId() + "-" + report.bizId())
                .build());
            return new MockExecutionResult("BUY", buy.order().bizId(), 1, buy.order().status(), buy.portfolio(),
                "已按报告参考配置金额执行模拟买入", targets.size());
        } catch (BusinessException exception) {
            String reason = "Mock 计划无法执行: " + exception.getMessage();
            log.warn("自动投资闭环Mock计划阻断: runNo={}, reportBizId={}, portfolioBizId={}, reason={}",
                run.runNo(), report.bizId(), portfolio.bizId(), reason);
            closedLoops.blockedStep(run, "MOCK_TRADE", "自动Mock交易", 70, reason,
                mapOfNullable("reportBizId", report.bizId(), "portfolioBizId", portfolio.bizId(),
                    "portfolioContext", parameters.get("portfolioContext"), "investmentPlan", report.investmentPlan()));
            throw new ClosedLoopBlockedException(reason);
        }
    }

    /** 判断报告方案是否明确要求本轮不交易。 */
    private boolean isNoTradeAction(String actionType) {
        return "HOLD".equals(actionType) || "SKIP".equals(actionType);
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
            log.info("自动投资闭环回测反馈开始: runNo={}, reportBizId={}, portfolioBizId={}",
                run.runNo(), report.bizId(), portfolio.bizId());
            BacktestResultView backtest = investmentClosedLoop.generateBacktestFromPortfolio(
                GenerateBacktestFromPortfolioCommand.builder()
                    .portfolioBizId(portfolio.bizId())
                    .strategyCode("AUTO_CLOSED_LOOP_MOCK")
                    .strategyVersion(run.runNo())
                    .benchmarkCode(nullableString(parameters, "benchmarkCode"))
                    .parameters(Jsons.toJson(Map.of(
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
                .metadata(Jsons.toJson(mapOfNullable(
                    "runBizId", run.bizId(),
                    "automationLevel", run.automationLevel(),
                    "qualityScore", report.dataQualityScore()
                )))
                .build());
            closedLoops.succeedStep(run, "BACKTEST_FEEDBACK", "回测与反馈沉淀", 80,
                Map.of("portfolioBizId", portfolio.bizId(), "reportBizId", report.bizId()),
                Map.of("backtestBizId", backtest.bizId(), "feedbackBizId", feedback.bizId()));
            log.info("自动投资闭环回测反馈完成: runNo={}, reportBizId={}, portfolioBizId={}, backtestBizId={}, feedbackBizId={}",
                run.runNo(), report.bizId(), portfolio.bizId(), backtest.bizId(), feedback.bizId());
            return backtest;
        });
    }

    /** 根据自动化开关处理 Prompt/模型正式启用；真实交易始终只记录闸门。 */
    private void recordActivationGuards(ClosedLoopRun run, Map<String, String> parameters, AiModel candidate) {
        if (TaskParameterParser.bool(parameters, "allowAutoPromptActivation", true)) {
            closedLoops.succeedStep(run, "PROMPT_ACTIVATION_GUARD", "Prompt正式启用闸门", 90,
                Map.of("allowAutoPromptActivation", true),
                Map.of(
                    "status", "ACTIVE",
                    "activationPolicy", "AUTO_PROMPT_GOVERNANCE_BASELINE_ACTIVE",
                    "summary", "Prompt治理任务已维护 ACTIVE 基线模板，自动闭环记录正式启用审计。"
                ));
        } else {
            closedLoops.skippedStep(run, "PROMPT_ACTIVATION_GUARD", "Prompt正式启用闸门", 90,
                "新 Prompt 正式启用开关关闭，本轮只保留候选与评分",
                Map.of("allowAutoPromptActivation", false));
        }
        if (TaskParameterParser.bool(parameters, "allowAutoModelActivation", true) && candidate != null) {
            AiModel activated = models.changeStatus(candidate.bizId(), "ACTIVE");
            closedLoops.succeedStep(run, "MODEL_ACTIVATION_GUARD", "模型正式启用闸门", 91,
                Map.of("allowAutoModelActivation", true, "modelCandidateBizId", candidate.bizId()),
                Map.of(
                    "modelBizId", activated.bizId(),
                    "modelCode", activated.modelCode(),
                    "modelVersion", activated.modelVersion(),
                    "status", activated.status()
                ));
        } else {
            closedLoops.skippedStep(run, "MODEL_ACTIVATION_GUARD", "模型正式启用闸门", 91,
                "新模型正式启用开关关闭或无候选模型，本轮只保留 DRAFT 候选",
                Map.of("allowAutoModelActivation", TaskParameterParser.bool(parameters, "allowAutoModelActivation", true),
                    "modelCandidateBizId", candidate == null ? "" : candidate.bizId()));
        }
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
        log.info(
            "自动投资闭环子任务开始: parentTaskCode={}, childTaskCode={}, childTaskType={}, overrides={}",
            parentEvent.taskCode(),
            definition.taskCode(),
            definition.taskType(),
            overrides
        );
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
        log.info(
            "自动投资闭环子任务完成: parentTaskCode={}, childTaskCode={}, childTaskType={}, eventId={}, status={}, failureReason={}",
            parentEvent.taskCode(),
            execution.taskCode(),
            execution.taskType(),
            execution.eventId(),
            execution.status(),
            execution.failureReason()
        );
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

    /** 拷贝存在的参数键，允许空字符串表达“清空子任务默认值”。 */
    private void copyIfKeyPresent(Map<String, String> source, Map<String, String> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, source.getOrDefault(key, ""));
        }
    }

    /** 解析报告方案动作类型，默认走买入兼容旧报告。 */
    private String actionType(InvestmentAnalysisReport report) {
        JsonNode plan = Jsons.readObjectOrEmpty(report.investmentPlan());
        String value = firstText(plan, "actionType", "suggestedActionType", "tradeAction");
        return value == null || value.isBlank() ? "BUY" : value.trim().toUpperCase();
    }

    /** 从报告方案中解析目标权重数组。 */
    private List<ExecuteMockRebalanceCommand.TargetWeight> targetWeights(InvestmentAnalysisReport report) {
        JsonNode plan = Jsons.readObjectOrEmpty(report.investmentPlan());
        JsonNode targets = firstArray(plan, "targetWeights", "rebalanceTargets", "targetPortfolio");
        if (targets == null || !targets.isArray()) {
            return List.of();
        }
        List<ExecuteMockRebalanceCommand.TargetWeight> result = new ArrayList<>();
        targets.forEach(target -> {
            String productBizId = firstText(target, "productBizId", "targetBizId", "bizId");
            BigDecimal weight = firstDecimal(target, "targetWeight", "weight", "allocationRate");
            if (productBizId != null && !productBizId.isBlank() && weight != null) {
                result.add(ExecuteMockRebalanceCommand.TargetWeight.builder()
                    .productBizId(productBizId.trim())
                    .targetWeight(weight.setScale(10, RoundingMode.HALF_UP))
                    .build());
            }
        });
        return result;
    }

    /** 读取第一个文本字段。 */
    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = Jsons.text(node, fieldName);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /** 读取第一个数组字段。 */
    private JsonNode firstArray(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode child = node == null ? null : node.get(fieldName);
            if (child != null && child.isArray()) {
                return child;
            }
        }
        return null;
    }

    /** 读取第一个数值字段。 */
    private BigDecimal firstDecimal(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            BigDecimal value = Jsons.decimal(node, fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
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

    /**
     * 对单份报告执行 Mock 前质量门禁检查，并输出前端可展示的阻断原因。
     *
     * @param report 待检查的投资报告
     * @param minQualityScore 本轮自动 Mock 最低质量分
     * @return 报告门禁检查结果
     * @author dz
     * @date 2026-06-26
     */
    private ReportGateCheck evaluateReportGate(InvestmentAnalysisReport report, BigDecimal minQualityScore) {
        List<String> reasons = new ArrayList<>();
        if (!isReportStatusSucceeded(report.status())) {
            reasons.add("REPORT_STATUS_NOT_SUCCEEDED");
        }
        if ("UNUSABLE".equals(report.confidenceLevel())) {
            reasons.add("CONFIDENCE_UNUSABLE");
        }
        if (report.dataQualityScore() == null) {
            reasons.add("QUALITY_SCORE_MISSING");
        } else if (report.dataQualityScore().compareTo(minQualityScore) < 0) {
            reasons.add("QUALITY_SCORE_BELOW_THRESHOLD");
        }
        if (!isDataQualityGatePassed(report)) {
            reasons.add("DATA_QUALITY_GATE_NOT_PASSED");
        }
        if (isDataGapReport(report)) {
            reasons.add("DATA_GAP_REPORT");
        }
        Map<String, Object> summary = mapOfNullable(
            "reportBizId", report.bizId(),
            "status", report.status(),
            "themeCode", report.themeCode(),
            "confidenceLevel", report.confidenceLevel(),
            "dataQualityScore", report.dataQualityScore(),
            "passed", reasons.isEmpty(),
            "blockedReasons", reasons
        );
        return new ReportGateCheck(report, reasons.isEmpty(), summary);
    }

    /** 判断报告数据质量门禁是否通过。 */
    private boolean isDataQualityGatePassed(InvestmentAnalysisReport report) {
        if (report.dataQualityGate() == null || report.dataQualityGate().isBlank()) {
            return false;
        }
        return Jsons.bool(Jsons.readObjectOrEmpty(report.dataQualityGate()), "passed", false);
    }

    /** 判断报告是否只是数据缺口报告。 */
    private boolean isDataGapReport(InvestmentAnalysisReport report) {
        if (report.investmentPlan() == null || report.investmentPlan().isBlank()) {
            return true;
        }
        var plan = Jsons.readObjectOrEmpty(report.investmentPlan());
        return "DATA_GAP_REPORT".equals(Jsons.text(plan, "planType"));
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

    /** 构造字符串参数 Map。 */
    private Map<String, String> mapOfString(String... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    /** 闭环门禁阻断异常，用于区分业务阻断和系统异常。 */
    private static class ClosedLoopBlockedException extends InvestmentTaskBlockedException {
        ClosedLoopBlockedException(String message) {
            super(message);
        }
    }

    /** 报告质量门禁检查结果。 */
    private record ReportGateCheck(InvestmentAnalysisReport report, boolean passed, Map<String, Object> summary) {
    }

    /** Mock 执行结果摘要。 */
    private record MockExecutionResult(
        String mode,
        String orderBizId,
        int orderCount,
        String status,
        MockPortfolioView portfolio,
        String reason,
        int targetWeightCount
    ) {
    }

    /** AI 结构化核心数据采集落库摘要。 */
    private record StructuredDataSummary(int newsCount, int productCount, int quoteCount) {
        private Map<String, Object> asMap() {
            return Map.of(
                "newsCount", newsCount,
                "productCount", productCount,
                "quoteCount", quoteCount
            );
        }
    }

    /** 确定性真实核心数据采集摘要。 */
    private record RealDataSummary(
        String taskType,
        int productCount,
        int quoteCount,
        int newsCount,
        BigDecimal qualityScore,
        boolean realTask,
        String summary
    ) {
        private Map<String, Object> asMap() {
            return Map.of(
                "taskType", taskType,
                "productCount", productCount,
                "quoteCount", quoteCount,
                "newsCount", newsCount,
                "qualityScore", qualityScore == null ? BigDecimal.ZERO : qualityScore,
                "summary", summary == null ? "" : summary
            );
        }
    }
}
