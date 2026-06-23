package com.example.dzcom.application.service.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveAiPromptEvaluationCommand;
import com.example.dzcom.application.command.ai.SaveBacktestResultCommand;
import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiPromptEvaluationView;
import com.example.dzcom.application.dto.ai.BacktestResultView;
import com.example.dzcom.application.dto.ai.InvestmentFeedbackView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.model.ai.AiPromptEvaluation;
import com.example.dzcom.domain.model.ai.BacktestResult;
import com.example.dzcom.domain.model.ai.InvestmentFeedback;
import com.example.dzcom.domain.model.portfolio.Portfolio;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationStore;
import com.example.dzcom.domain.repository.ai.BacktestResultSearchCriteria;
import com.example.dzcom.domain.repository.ai.BacktestResultStore;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioValuationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 投资报告、回测、用户反馈和 Prompt 评估闭环应用服务。 */
@Service
@RequiredArgsConstructor
public class InvestmentClosedLoopApplicationService {
    private static final Set<String> BACKTEST_STATUSES =
        Set.of("PENDING", "RUNNING", "SUCCEEDED", "FAILED", "CANCELLED");
    private static final Set<String> FEEDBACK_ACTIONS = Set.of("ADOPT", "REJECT", "WATCH", "IGNORE");
    private static final Set<String> TARGET_TYPES =
        Set.of("REPORT", "RECOMMENDATION", "MOCK_ORDER", "MOCK_PORTFOLIO", "BACKTEST", "PROMPT");
    private static final Set<String> REVIEW_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED", "ARCHIVED");
    private static final Set<String> BACKTEST_SORTS =
        Set.of("createdAt", "strategyCode", "strategyVersion", "status", "startDate", "endDate");
    private static final Set<String> FEEDBACK_SORTS =
        Set.of("createdAt", "targetType", "feedbackAction", "promptCode", "promptVersion");
    private static final Set<String> EVALUATION_SORTS =
        Set.of("evaluatedAt", "promptCode", "promptVersion", "scenario", "score", "reviewStatus");

    private final BacktestResultStore backtests;
    private final InvestmentFeedbackStore feedbacks;
    private final AiPromptEvaluationStore promptEvaluations;
    private final PortfolioStore portfolios;
    private final PortfolioValuationStore valuations;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 保存外部或后台计算得到的回测结果。
     *
     * @param command 回测保存命令
     * @return 保存后的回测结果视图
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public BacktestResultView saveBacktest(SaveBacktestResultCommand command) {
        CurrentOperator operator = currentOperator.required();
        validateJson(command.parameters(), "回测参数JSON不能为空");
        validateOptionalJson(command.metrics(), "回测指标JSON格式不合法");
        LocalDateTime now = clock.now();
        BacktestResult existing = command.bizId() == null || command.bizId().isBlank()
            ? null
            : requiredOwnedBacktest(command.bizId(), operator.userBizId());
        BacktestResult result = BacktestResult.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .ownerUserBizId(existing == null ? operator.userBizId() : existing.ownerUserBizId())
            .strategyCode(normalizeCode(command.strategyCode(), "策略编码不能为空"))
            .strategyVersion(normalizeText(command.strategyVersion(), "策略版本不能为空"))
            .startDate(requireDate(command.startDate(), "回测开始日期不能为空"))
            .endDate(requireDate(command.endDate(), "回测结束日期不能为空"))
            .initialCapital(normalizePositive(command.initialCapital(), "初始资金必须大于0"))
            .benchmarkCode(trimToNull(command.benchmarkCode()))
            .parameters(command.parameters().trim())
            .metrics(trimToNull(command.metrics()))
            .resultUri(trimToNull(command.resultUri()))
            .status(normalizeAllowed(defaultIfBlank(command.status(), "PENDING"), BACKTEST_STATUSES, "回测状态不合法"))
            .failureReason(trimToNull(command.failureReason()))
            .startedAt(command.startedAt())
            .completedAt(command.completedAt())
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .build();
        validateDateRange(result.startDate(), result.endDate());
        return toBacktestView(backtests.save(result));
    }

    /**
     * 从 Mock 组合估值曲线生成一条回测摘要。
     *
     * @param command 从组合生成回测摘要命令
     * @return 生成后的回测结果视图
     * @throws BusinessException 当估值点不足时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public BacktestResultView generateBacktestFromPortfolio(GenerateBacktestFromPortfolioCommand command) {
        CurrentOperator operator = currentOperator.required();
        String portfolioBizId = normalizeText(command.portfolioBizId(), "模拟组合不能为空");
        Portfolio portfolio = requiredOwnedPortfolio(portfolioBizId, operator.userBizId());
        int limit = normalizeLimit(command.limit());
        validateOptionalJson(command.parameters(), "回测参数JSON格式不合法");
        List<PortfolioValuation> history = valuations.findHistoryByPortfolioBizId(portfolioBizId, limit);
        if (history.size() < 2) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "估值点不足，不能生成回测摘要");
        }
        PortfolioValuation first = history.get(0);
        PortfolioValuation last = history.get(history.size() - 1);
        BigDecimal totalReturn = returnRate(last.totalAsset(), first.totalAsset());
        BigDecimal maxDrawdown = maxDrawdown(history);
        BigDecimal volatility = simpleVolatility(history);
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("source", "MOCK_PORTFOLIO_VALUATION");
        parameterMap.put("portfolioBizId", portfolio.bizId());
        parameterMap.put("portfolioName", portfolio.portfolioName());
        parameterMap.put("pointCount", history.size());
        if (command.parameters() != null && !command.parameters().isBlank()) {
            parameterMap.put("clientParameters", JSON.parse(command.parameters()));
        }
        String parameters = JSON.toJSONString(parameterMap);
        String metrics = JSON.toJSONString(Map.of(
            "totalReturnRate", totalReturn,
            "maxDrawdown", maxDrawdown,
            "volatility", volatility,
            "startAsset", first.totalAsset(),
            "endAsset", last.totalAsset(),
            "pointCount", history.size()
        ));
        LocalDateTime now = clock.now();
        BacktestResult result = BacktestResult.builder()
            .bizId(ids.newBizId())
            .ownerUserBizId(operator.userBizId())
            .strategyCode(normalizeCode(command.strategyCode(), "策略编码不能为空"))
            .strategyVersion(normalizeText(command.strategyVersion(), "策略版本不能为空"))
            .startDate(first.valuationTime().toLocalDate())
            .endDate(last.valuationTime().toLocalDate())
            .initialCapital(first.totalAsset())
            .benchmarkCode(trimToNull(command.benchmarkCode()))
            .parameters(parameters)
            .metrics(metrics)
            .resultUri(null)
            .status("SUCCEEDED")
            .failureReason(null)
            .startedAt(first.valuationTime())
            .completedAt(last.valuationTime())
            .createdAt(now)
            .updatedAt(now)
            .build();
        return toBacktestView(backtests.save(result));
    }

    /** 查询回测详情。 */
    @Transactional(readOnly = true)
    public BacktestResultView backtestDetail(String bizId) {
        CurrentOperator operator = currentOperator.required();
        return toBacktestView(requiredOwnedBacktest(bizId, operator.userBizId()));
    }

    /** 分页查询回测结果。 */
    @Transactional(readOnly = true)
    public PageResult<BacktestResultView> listBacktests(String strategyCode, String strategyVersion,
                                                        String status, PageQuery query) {
        CurrentOperator operator = currentOperator.required();
        PageResult<BacktestResult> page = backtests.search(new BacktestResultSearchCriteria(
            operator.userBizId(),
            trimToNull(strategyCode),
            trimToNull(strategyVersion),
            trimToNull(status),
            query.page(),
            query.size(),
            query.safeSort(BACKTEST_SORTS, "createdAt"),
            "asc".equals(query.direction())
        ));
        return mapBacktestPage(page);
    }

    /**
     * 保存用户或人工复核反馈，并在携带 Prompt 信息时生成自动评估。
     *
     * @param command 反馈保存命令
     * @return 保存后的反馈视图
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public InvestmentFeedbackView saveFeedback(SaveInvestmentFeedbackCommand command) {
        CurrentOperator operator = currentOperator.required();
        validateOptionalJson(command.metadata(), "反馈上下文JSON格式不合法");
        requireOwnedBacktestIfPresent(command.backtestBizId(), operator.userBizId());
        LocalDateTime now = clock.now();
        InvestmentFeedback feedback = InvestmentFeedback.builder()
            .bizId(ids.newBizId())
            .userBizId(operator.userBizId())
            .targetType(normalizeAllowed(command.targetType(), TARGET_TYPES, "反馈目标类型不合法"))
            .targetBizId(normalizeText(command.targetBizId(), "反馈目标不能为空"))
            .reportBizId(trimToNull(command.reportBizId()))
            .promptBizId(trimToNull(command.promptBizId()))
            .promptCode(trimToNull(command.promptCode()))
            .promptVersion(trimToNull(command.promptVersion()))
            .backtestBizId(trimToNull(command.backtestBizId()))
            .feedbackAction(normalizeAllowed(command.feedbackAction(), FEEDBACK_ACTIONS, "反馈动作不合法"))
            .reasonCode(trimToNull(command.reasonCode()))
            .commentText(trimToNull(command.commentText()))
            .metadata(trimToNull(command.metadata()))
            .createdAt(now)
            .build();
        InvestmentFeedback saved = feedbacks.save(feedback);
        autoEvaluatePrompt(saved, operator.userBizId());
        return toFeedbackView(saved);
    }

    /** 查询反馈详情。 */
    @Transactional(readOnly = true)
    public InvestmentFeedbackView feedbackDetail(String bizId) {
        CurrentOperator operator = currentOperator.required();
        return toFeedbackView(requiredOwnedFeedback(bizId, operator.userBizId()));
    }

    /** 分页查询反馈。 */
    @Transactional(readOnly = true)
    public PageResult<InvestmentFeedbackView> listFeedback(String targetType, String targetBizId,
                                                           String reportBizId, String promptCode,
                                                           String promptVersion, String backtestBizId,
                                                           String feedbackAction, PageQuery query) {
        CurrentOperator operator = currentOperator.required();
        PageResult<InvestmentFeedback> page = feedbacks.search(new InvestmentFeedbackSearchCriteria(
            operator.userBizId(),
            trimToNull(targetType),
            trimToNull(targetBizId),
            trimToNull(reportBizId),
            trimToNull(promptCode),
            trimToNull(promptVersion),
            trimToNull(backtestBizId),
            trimToNull(feedbackAction),
            query.page(),
            query.size(),
            query.safeSort(FEEDBACK_SORTS, "createdAt"),
            "asc".equals(query.direction())
        ));
        return PageResult.<InvestmentFeedbackView>builder()
            .items(page.items().stream().map(this::toFeedbackView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /** 保存 Prompt 评估。 */
    @Transactional
    public AiPromptEvaluationView savePromptEvaluation(SaveAiPromptEvaluationCommand command) {
        CurrentOperator operator = currentOperator.required();
        validateOptionalJson(command.scoreDetail(), "评分详情JSON格式不合法");
        requireOwnedBacktestIfPresent(command.backtestBizId(), operator.userBizId());
        requireOwnedFeedbackIfPresent(command.feedbackBizId(), operator.userBizId());
        LocalDateTime now = clock.now();
        AiPromptEvaluation evaluation = AiPromptEvaluation.builder()
            .bizId(ids.newBizId())
            .promptBizId(trimToNull(command.promptBizId()))
            .promptCode(normalizeCode(command.promptCode(), "Prompt编码不能为空"))
            .promptVersion(normalizeText(command.promptVersion(), "Prompt版本不能为空"))
            .scenario(normalizeCode(command.scenario(), "Prompt场景不能为空"))
            .backtestBizId(trimToNull(command.backtestBizId()))
            .feedbackBizId(trimToNull(command.feedbackBizId()))
            .score(normalizeRatio(command.score(), "Prompt评估分必须在0到1之间"))
            .scoreDetail(trimToNull(command.scoreDetail()))
            .reviewStatus(normalizeAllowed(defaultIfBlank(command.reviewStatus(), "PENDING"), REVIEW_STATUSES, "复核状态不合法"))
            .evaluatorType("ADMIN")
            .evaluatorBizId(operator.userBizId())
            .evaluatedAt(now)
            .createdAt(now)
            .build();
        return toEvaluationView(promptEvaluations.save(evaluation));
    }

    /** 查询 Prompt 评估详情。 */
    @Transactional(readOnly = true)
    public AiPromptEvaluationView promptEvaluationDetail(String bizId) {
        CurrentOperator operator = currentOperator.required();
        AiPromptEvaluation evaluation = promptEvaluations.findByBizId(normalizeText(bizId, "Prompt评估业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Prompt评估不存在"));
        requireEvaluationVisible(evaluation, operator.userBizId());
        return toEvaluationView(evaluation);
    }

    /** 分页查询 Prompt 评估。 */
    @Transactional(readOnly = true)
    public PageResult<AiPromptEvaluationView> listPromptEvaluations(String promptCode, String promptVersion,
                                                                    String scenario, String backtestBizId,
                                                                    String feedbackBizId, String reviewStatus,
                                                                    PageQuery query) {
        CurrentOperator operator = currentOperator.required();
        PageResult<AiPromptEvaluation> page = promptEvaluations.search(new AiPromptEvaluationSearchCriteria(
            operator.userBizId(),
            trimToNull(promptCode),
            trimToNull(promptVersion),
            trimToNull(scenario),
            trimToNull(backtestBizId),
            trimToNull(feedbackBizId),
            trimToNull(reviewStatus),
            query.page(),
            query.size(),
            query.safeSort(EVALUATION_SORTS, "evaluatedAt"),
            "asc".equals(query.direction())
        ));
        return PageResult.<AiPromptEvaluationView>builder()
            .items(page.items().stream().map(this::toEvaluationView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /** 根据反馈动作和回测结果自动生成 Prompt 评估。 */
    private void autoEvaluatePrompt(InvestmentFeedback feedback, String evaluatorBizId) {
        if (feedback.promptCode() == null || feedback.promptVersion() == null) {
            return;
        }
        BigDecimal feedbackScore = switch (feedback.feedbackAction()) {
            case "ADOPT" -> new BigDecimal("0.90");
            case "WATCH" -> new BigDecimal("0.65");
            case "IGNORE" -> new BigDecimal("0.45");
            default -> new BigDecimal("0.20");
        };
        BigDecimal backtestScore = feedback.backtestBizId() == null
            ? feedbackScore
            : backtests.findByBizId(feedback.backtestBizId())
                .map(this::scoreBacktest)
                .orElse(feedbackScore);
        BigDecimal finalScore = feedbackScore.add(backtestScore).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        LocalDateTime now = clock.now();
        promptEvaluations.save(AiPromptEvaluation.builder()
            .bizId(ids.newBizId())
            .promptBizId(feedback.promptBizId())
            .promptCode(feedback.promptCode())
            .promptVersion(feedback.promptVersion())
            .scenario("FEEDBACK_LOOP")
            .backtestBizId(feedback.backtestBizId())
            .feedbackBizId(feedback.bizId())
            .score(finalScore)
            .scoreDetail(JSON.toJSONString(Map.of(
                "feedbackAction", feedback.feedbackAction(),
                "feedbackScore", feedbackScore,
                "backtestScore", backtestScore
            )))
            .reviewStatus("PENDING")
            .evaluatorType("SYSTEM")
            .evaluatorBizId(evaluatorBizId)
            .evaluatedAt(now)
            .createdAt(now)
            .build());
    }

    /**
     * 查询并校验回测结果归属当前用户。
     *
     * @param bizId 回测业务唯一标识
     * @param userBizId 当前用户业务标识
     * @return 归属于当前用户的回测结果
     * @throws BusinessException 当回测不存在或不属于当前用户时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BacktestResult requiredOwnedBacktest(String bizId, String userBizId) {
        BacktestResult result = backtests.findByBizId(normalizeText(bizId, "回测业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "回测结果不存在"));
        if (!userBizId.equals(result.ownerUserBizId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权访问该回测结果");
        }
        return result;
    }

    /**
     * 查询并校验模拟组合归属当前用户。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param userBizId 当前用户业务标识
     * @return 归属于当前用户的模拟组合
     * @throws BusinessException 当组合不存在或不属于当前用户时抛出
     * @author dz
     * @date 2026-06-23
     */
    private Portfolio requiredOwnedPortfolio(String portfolioBizId, String userBizId) {
        Portfolio portfolio = portfolios.findByBizId(portfolioBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模拟组合不存在"));
        if (!userBizId.equals(portfolio.ownerUserBizId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权访问该模拟组合");
        }
        return portfolio;
    }

    /**
     * 查询并校验反馈记录归属当前用户。
     *
     * @param bizId 反馈业务唯一标识
     * @param userBizId 当前用户业务标识
     * @return 归属于当前用户的反馈记录
     * @throws BusinessException 当反馈不存在或不属于当前用户时抛出
     * @author dz
     * @date 2026-06-23
     */
    private InvestmentFeedback requiredOwnedFeedback(String bizId, String userBizId) {
        InvestmentFeedback feedback = feedbacks.findByBizId(normalizeText(bizId, "反馈业务ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "反馈不存在"));
        if (!userBizId.equals(feedback.userBizId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权访问该反馈");
        }
        return feedback;
    }

    /**
     * 当请求携带回测业务标识时校验归属。
     *
     * @param backtestBizId 回测业务唯一标识
     * @param userBizId 当前用户业务标识
     * @author dz
     * @date 2026-06-23
     */
    private void requireOwnedBacktestIfPresent(String backtestBizId, String userBizId) {
        if (backtestBizId != null && !backtestBizId.isBlank()) {
            requiredOwnedBacktest(backtestBizId, userBizId);
        }
    }

    /**
     * 当请求携带反馈业务标识时校验归属。
     *
     * @param feedbackBizId 反馈业务唯一标识
     * @param userBizId 当前用户业务标识
     * @author dz
     * @date 2026-06-23
     */
    private void requireOwnedFeedbackIfPresent(String feedbackBizId, String userBizId) {
        if (feedbackBizId != null && !feedbackBizId.isBlank()) {
            requiredOwnedFeedback(feedbackBizId, userBizId);
        }
    }

    /**
     * 校验 Prompt 评估记录对当前用户可见。
     *
     * @param evaluation Prompt 评估记录
     * @param userBizId 当前用户业务标识
     * @throws BusinessException 当评估记录关联了当前用户不可见的数据时抛出
     * @author dz
     * @date 2026-06-23
     */
    private void requireEvaluationVisible(AiPromptEvaluation evaluation, String userBizId) {
        if (userBizId.equals(evaluation.evaluatorBizId())) {
            return;
        }
        if (evaluation.feedbackBizId() != null) {
            requiredOwnedFeedback(evaluation.feedbackBizId(), userBizId);
            return;
        }
        if (evaluation.backtestBizId() != null) {
            requiredOwnedBacktest(evaluation.backtestBizId(), userBizId);
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, "无权访问该Prompt评估");
    }

    /** 根据回测指标计算简化评分。 */
    private BigDecimal scoreBacktest(BacktestResult result) {
        if (!"SUCCEEDED".equals(result.status()) || result.metrics() == null) {
            return new BigDecimal("0.20");
        }
        try {
            var metrics = JSON.parseObject(result.metrics());
            BigDecimal totalReturn = metrics.getBigDecimal("totalReturnRate");
            BigDecimal maxDrawdown = metrics.getBigDecimal("maxDrawdown");
            BigDecimal base = new BigDecimal("0.50");
            if (totalReturn != null) {
                base = base.add(totalReturn.min(new BigDecimal("0.30")).max(new BigDecimal("-0.30")));
            }
            if (maxDrawdown != null) {
                base = base.subtract(maxDrawdown.min(new BigDecimal("0.30")));
            }
            return base.max(BigDecimal.ZERO).min(BigDecimal.ONE).setScale(4, RoundingMode.HALF_UP);
        } catch (JSONException ex) {
            return new BigDecimal("0.20");
        }
    }

    private PageResult<BacktestResultView> mapBacktestPage(PageResult<BacktestResult> page) {
        return PageResult.<BacktestResultView>builder()
            .items(page.items().stream().map(this::toBacktestView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    private BacktestResultView toBacktestView(BacktestResult result) {
        return BacktestResultView.builder()
            .bizId(result.bizId())
            .ownerUserBizId(result.ownerUserBizId())
            .strategyCode(result.strategyCode())
            .strategyVersion(result.strategyVersion())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .initialCapital(result.initialCapital())
            .benchmarkCode(result.benchmarkCode())
            .parameters(result.parameters())
            .metrics(result.metrics())
            .resultUri(result.resultUri())
            .status(result.status())
            .failureReason(result.failureReason())
            .startedAt(result.startedAt())
            .completedAt(result.completedAt())
            .createdAt(result.createdAt())
            .updatedAt(result.updatedAt())
            .build();
    }

    private InvestmentFeedbackView toFeedbackView(InvestmentFeedback feedback) {
        return InvestmentFeedbackView.builder()
            .bizId(feedback.bizId())
            .userBizId(feedback.userBizId())
            .targetType(feedback.targetType())
            .targetBizId(feedback.targetBizId())
            .reportBizId(feedback.reportBizId())
            .promptBizId(feedback.promptBizId())
            .promptCode(feedback.promptCode())
            .promptVersion(feedback.promptVersion())
            .backtestBizId(feedback.backtestBizId())
            .feedbackAction(feedback.feedbackAction())
            .reasonCode(feedback.reasonCode())
            .commentText(feedback.commentText())
            .metadata(feedback.metadata())
            .createdAt(feedback.createdAt())
            .build();
    }

    private AiPromptEvaluationView toEvaluationView(AiPromptEvaluation evaluation) {
        return AiPromptEvaluationView.builder()
            .bizId(evaluation.bizId())
            .promptBizId(evaluation.promptBizId())
            .promptCode(evaluation.promptCode())
            .promptVersion(evaluation.promptVersion())
            .scenario(evaluation.scenario())
            .backtestBizId(evaluation.backtestBizId())
            .feedbackBizId(evaluation.feedbackBizId())
            .score(evaluation.score())
            .scoreDetail(evaluation.scoreDetail())
            .reviewStatus(evaluation.reviewStatus())
            .evaluatorType(evaluation.evaluatorType())
            .evaluatorBizId(evaluation.evaluatorBizId())
            .evaluatedAt(evaluation.evaluatedAt())
            .createdAt(evaluation.createdAt())
            .build();
    }

    private BigDecimal returnRate(BigDecimal currentAsset, BigDecimal baseAsset) {
        if (baseAsset == null || baseAsset.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentAsset.subtract(baseAsset).divide(baseAsset, 10, RoundingMode.HALF_UP);
    }

    private BigDecimal maxDrawdown(List<PortfolioValuation> history) {
        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        for (PortfolioValuation valuation : history) {
            BigDecimal totalAsset = valuation.totalAsset();
            if (totalAsset.compareTo(peak) > 0) {
                peak = totalAsset;
            }
            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(totalAsset).divide(peak, 10, RoundingMode.HALF_UP);
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                }
            }
        }
        return maxDrawdown;
    }

    private BigDecimal simpleVolatility(List<PortfolioValuation> history) {
        if (history.size() < 2) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> returns = new java.util.ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            returns.add(returnRate(history.get(i).totalAsset(), history.get(i - 1).totalAsset()));
        }
        BigDecimal average = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 10, RoundingMode.HALF_UP);
        BigDecimal variance = returns.stream()
            .map(value -> value.subtract(average).multiply(value.subtract(average)))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 10, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(10, RoundingMode.HALF_UP);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 500;
        }
        if (limit < 2 || limit > 2000) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "回测估值点数量必须在2到2000之间");
        }
        return limit;
    }

    private BigDecimal normalizePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeRatio(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private LocalDate requireDate(LocalDate value, String message) {
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "回测结束日期不能早于开始日期");
        }
    }

    private void validateJson(String value, String message) {
        String text = normalizeText(value, message);
        try {
            JSON.parse(text);
        } catch (JSONException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateOptionalJson(String value, String message) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            JSON.parse(value);
        } catch (JSONException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private String normalizeAllowed(String value, Set<String> allowed, String message) {
        String normalized = normalizeCode(value, message);
        if (!allowed.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeCode(String value, String message) {
        return normalizeText(value, message).toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return trimmed;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
