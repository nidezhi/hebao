package com.example.dzcom.application.service.analytics;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.dto.analytics.InvestmentEvolutionSummaryView;
import com.example.dzcom.domain.model.ai.AiModelCallAudit;
import com.example.dzcom.domain.model.ai.BacktestResult;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.ai.InvestmentFeedback;
import com.example.dzcom.domain.model.portfolio.OrderEvent;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.model.risk.RiskCheck;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditStore;
import com.example.dzcom.domain.repository.ai.BacktestResultSearchCriteria;
import com.example.dzcom.domain.repository.ai.BacktestResultStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackStore;
import com.example.dzcom.domain.repository.portfolio.OrderEventStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioValuationStore;
import com.example.dzcom.domain.repository.risk.RiskCheckSearchCriteria;
import com.example.dzcom.domain.repository.risk.RiskCheckStore;
import com.example.dzcom.domain.repository.task.ClosedLoopRunSearchCriteria;
import com.example.dzcom.domain.repository.task.ClosedLoopRunStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 投资闭环持续进化分析应用服务。
 *
 * <p>服务只读取现有闭环产物和审计流水，不改变任务执行状态；当样本量不足或某类数据缺失时，
 * 通过 {@code limitations} 和 {@code sampleStatus} 明确暴露证据边界。</p>
 */
@Service
@RequiredArgsConstructor
public class InvestmentEvolutionAnalyticsApplicationService {
    private static final int MAX_SAMPLE_SIZE = 100;
    private static final int MIN_TRUST_SAMPLE_SIZE = 30;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final ClosedLoopRunStore closedLoops;
    private final InvestmentAnalysisReportStore reports;
    private final PortfolioValuationStore valuations;
    private final OrderEventStore orderEvents;
    private final RiskCheckStore riskChecks;
    private final AiModelCallAuditStore modelCallAudits;
    private final InvestmentFeedbackStore feedbacks;
    private final BacktestResultStore backtests;
    private final ClockProvider clock;

    /**
     * 汇总最近样本的闭环进化指标。
     *
     * @param requestedSampleSize 请求的样本窗口大小，服务会限制在 1-100
     * @return 可供前端统一分析页展示的进化分析视图
     */
    @Transactional(readOnly = true)
    public InvestmentEvolutionSummaryView summary(Integer requestedSampleSize) {
        int sampleSize = sampleSize(requestedSampleSize);
        List<ClosedLoopRun> runs = recentRuns(sampleSize);
        List<InvestmentAnalysisReport> latestReports = reports.latest(sampleSize).items();
        List<AiModelCallAudit> audits = recentModelAudits(sampleSize);
        List<RiskCheck> risks = recentRiskChecks(sampleSize);
        List<InvestmentFeedback> latestFeedbacks = recentFeedbacks(sampleSize);
        List<BacktestResult> latestBacktests = recentBacktests(sampleSize);
        PortfolioWindow portfolioWindow = portfolioWindow(runs);
        InvestmentEvolutionSummaryView.ClosedLoopMetricsView closedLoopMetrics =
            closedLoopMetrics(runs, latestReports);
        InvestmentEvolutionSummaryView.PortfolioMetricsView portfolioMetrics =
            portfolioMetrics(portfolioWindow);
        InvestmentEvolutionSummaryView.RiskMetricsView riskMetrics = riskMetrics(risks);
        InvestmentEvolutionSummaryView.ModelMetricsView modelMetrics = modelMetrics(audits);
        InvestmentEvolutionSummaryView.FeedbackMetricsView feedbackMetrics =
            feedbackMetrics(latestFeedbacks, latestBacktests);
        List<String> limitations = limitations(runs, latestReports, audits, risks, portfolioWindow);
        return InvestmentEvolutionSummaryView.builder()
            .generatedAt(clock.now())
            .sampleStatus(sampleStatus(runs.size(), audits.size(), portfolioWindow.valuationPointCount()))
            .sampleWindowSize(sampleSize)
            .kpis(kpis(closedLoopMetrics, portfolioMetrics, riskMetrics, modelMetrics, feedbackMetrics))
            .closedLoop(closedLoopMetrics)
            .portfolio(portfolioMetrics)
            .risk(riskMetrics)
            .model(modelMetrics)
            .feedback(feedbackMetrics)
            .variants(variants(audits))
            .recentRuns(recentRunViews(runs))
            .limitations(limitations)
            .build();
    }

    /**
     * 查询最近闭环运行样本。
     *
     * @param sampleSize 样本窗口大小
     * @return 最近闭环运行
     */
    private List<ClosedLoopRun> recentRuns(int sampleSize) {
        PageResult<ClosedLoopRun> page = closedLoops.searchRuns(new ClosedLoopRunSearchCriteria(
            null, null, null, null, null, null,
            null, null, 1, sampleSize, "startedAt", false
        ));
        return page.items();
    }

    /**
     * 查询最近模型调用审计样本。
     *
     * @param sampleSize 样本窗口大小
     * @return 最近模型调用审计
     */
    private List<AiModelCallAudit> recentModelAudits(int sampleSize) {
        return modelCallAudits.search(new AiModelCallAuditSearchCriteria(
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            1, sampleSize, "createdAt", false
        )).items();
    }

    /**
     * 查询最近风控审计样本。
     *
     * @param sampleSize 样本窗口大小
     * @return 最近风控记录
     */
    private List<RiskCheck> recentRiskChecks(int sampleSize) {
        return riskChecks.search(new RiskCheckSearchCriteria(
            null, null, null, null, null, null,
            1, sampleSize, "checkedAt", false
        )).items();
    }

    /**
     * 查询最近反馈样本。
     *
     * @param sampleSize 样本窗口大小
     * @return 最近反馈记录
     */
    private List<InvestmentFeedback> recentFeedbacks(int sampleSize) {
        return feedbacks.search(new InvestmentFeedbackSearchCriteria(
            null, null, null, null, null, null, null, null,
            1, sampleSize, "createdAt", false
        )).items();
    }

    /**
     * 查询最近回测样本。
     *
     * @param sampleSize 样本窗口大小
     * @return 最近回测记录
     */
    private List<BacktestResult> recentBacktests(int sampleSize) {
        return backtests.search(new BacktestResultSearchCriteria(
            null, null, null, null,
            1, sampleSize, "createdAt", false
        )).items();
    }

    /**
     * 归集闭环指标。
     *
     * @param runs 闭环运行样本
     * @param latestReports 报告样本
     * @return 闭环指标
     */
    private InvestmentEvolutionSummaryView.ClosedLoopMetricsView closedLoopMetrics(
        List<ClosedLoopRun> runs,
        List<InvestmentAnalysisReport> latestReports
    ) {
        return InvestmentEvolutionSummaryView.ClosedLoopMetricsView.builder()
            .sampleCount(runs.size())
            .successCount(count(runs, run -> isAny(run.runStatus(), "SUCCEEDED", "COMPLETED", "SUCCESS")))
            .failedCount(count(runs, run -> isAny(run.runStatus(), "FAILED", "ERROR")))
            .runningCount(count(runs, run -> "RUNNING".equalsIgnoreCase(run.runStatus())))
            .blockedCount(count(runs, run -> isAny(run.runStatus(), "BLOCKED", "SKIPPED")
                || "BLOCK".equalsIgnoreCase(run.gateResult())))
            .successRate(rate(count(runs, run -> isAny(run.runStatus(), "SUCCEEDED", "COMPLETED", "SUCCESS")),
                runs.size()))
            .averageQualityScore(average(latestReports.stream()
                .map(InvestmentAnalysisReport::dataQualityScore)
                .filter(Objects::nonNull)
                .toList()))
            .gatePassCount(count(runs, run -> isAny(run.gateResult(), "PASS", "PASSED")))
            .build();
    }

    /**
     * 归集 Mock 组合窗口样本。
     *
     * @param runs 闭环运行样本
     * @return 组合估值和订单事件窗口
     */
    private PortfolioWindow portfolioWindow(List<ClosedLoopRun> runs) {
        List<String> portfolioBizIds = runs.stream()
            .map(ClosedLoopRun::portfolioBizId)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .limit(5)
            .toList();
        List<PortfolioValuation> allValuations = new ArrayList<>();
        List<OrderEvent> allEvents = new ArrayList<>();
        for (String portfolioBizId : portfolioBizIds) {
            allValuations.addAll(valuations.findHistoryByPortfolioBizId(portfolioBizId, MAX_SAMPLE_SIZE));
            allEvents.addAll(orderEvents.findRecentByPortfolioBizId(portfolioBizId, MAX_SAMPLE_SIZE));
        }
        return new PortfolioWindow(portfolioBizIds, allValuations, allEvents);
    }

    /**
     * 归集 Mock 组合指标。
     *
     * @param window 组合估值和订单事件窗口
     * @return 组合指标
     */
    private InvestmentEvolutionSummaryView.PortfolioMetricsView portfolioMetrics(PortfolioWindow window) {
        List<PortfolioValuation> sorted = window.valuations().stream()
            .sorted(Comparator.comparing(PortfolioValuation::valuationTime))
            .toList();
        BigDecimal latestReturn = sorted.isEmpty()
            ? null
            : sorted.get(sorted.size() - 1).totalReturnRate();
        int filledEvents = (int) window.events().stream()
            .filter(event -> isAny(event.eventType(), "FILLED") || isAny(event.toStatus(), "FILLED"))
            .count();
        return InvestmentEvolutionSummaryView.PortfolioMetricsView.builder()
            .portfolioCount(window.portfolioBizIds().size())
            .valuationPointCount(sorted.size())
            .latestReturnRate(scale(latestReturn))
            .maxDrawdown(maxDrawdown(sorted))
            .orderEventCount(window.events().size())
            .filledOrderEventCount(filledEvents)
            .turnoverProxy(rate(filledEvents, Math.max(1, sorted.size())))
            .build();
    }

    /**
     * 归集风控指标。
     *
     * @param risks 风控样本
     * @return 风控指标
     */
    private InvestmentEvolutionSummaryView.RiskMetricsView riskMetrics(List<RiskCheck> risks) {
        Map<String, Long> reasons = risks.stream()
            .filter(risk -> isAny(risk.checkResult(), "REJECT", "REJECTED", "BLOCK"))
            .map(risk -> blankTo(risk.reasonCode(), "UNKNOWN"))
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        return InvestmentEvolutionSummaryView.RiskMetricsView.builder()
            .sampleCount(risks.size())
            .passCount(count(risks, risk -> isAny(risk.checkResult(), "PASS", "PASSED")))
            .reviewCount(count(risks, risk -> isAny(risk.checkResult(), "REVIEW", "MANUAL_REVIEW")))
            .rejectCount(count(risks, risk -> isAny(risk.checkResult(), "REJECT", "REJECTED", "BLOCK")))
            .topRejectReasons(reasons.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> InvestmentEvolutionSummaryView.ReasonCountView.builder()
                    .reasonCode(entry.getKey())
                    .count(entry.getValue())
                    .build())
                .toList())
            .build();
    }

    /**
     * 归集模型调用稳定性指标。
     *
     * @param audits 模型调用审计样本
     * @return 模型指标
     */
    private InvestmentEvolutionSummaryView.ModelMetricsView modelMetrics(List<AiModelCallAudit> audits) {
        long success = count(audits, audit -> isAny(audit.callStatus(), "SUCCEEDED", "SUCCESS"));
        long failed = count(audits, audit -> isAny(audit.callStatus(), "FAILED", "ERROR", "BLOCKED"));
        return InvestmentEvolutionSummaryView.ModelMetricsView.builder()
            .sampleCount(audits.size())
            .successCount(success)
            .failedCount(failed)
            .successRate(rate(success, audits.size()))
            .averageDurationMs(average(audits.stream()
                .map(AiModelCallAudit::durationMs)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .toList()))
            .build();
    }

    /**
     * 归集反馈和回测指标。
     *
     * @param latestFeedbacks 反馈样本
     * @param latestBacktests 回测样本
     * @return 反馈与回测指标
     */
    private InvestmentEvolutionSummaryView.FeedbackMetricsView feedbackMetrics(
        List<InvestmentFeedback> latestFeedbacks,
        List<BacktestResult> latestBacktests
    ) {
        return InvestmentEvolutionSummaryView.FeedbackMetricsView.builder()
            .feedbackCount(latestFeedbacks.size())
            .positiveFeedbackCount(count(latestFeedbacks, feedback ->
                isAny(feedback.feedbackAction(), "ACCEPT", "APPROVE", "POSITIVE", "KEEP")))
            .negativeFeedbackCount(count(latestFeedbacks, feedback ->
                isAny(feedback.feedbackAction(), "REJECT", "NEGATIVE", "ADJUST", "ROLLBACK")))
            .backtestCount(latestBacktests.size())
            .completedBacktestCount(count(latestBacktests, backtest ->
                isAny(backtest.status(), "COMPLETED", "SUCCEEDED", "SUCCESS")))
            .build();
    }

    /**
     * 生成 A/B 归因变体表现。
     *
     * @param audits 模型调用审计样本
     * @return 按模型、Prompt、Skill、场景聚合后的变体表现
     */
    private List<InvestmentEvolutionSummaryView.VariantPerformanceView> variants(List<AiModelCallAudit> audits) {
        return audits.stream()
            .collect(Collectors.groupingBy(this::variantKey, LinkedHashMap::new, Collectors.toList()))
            .entrySet()
            .stream()
            .map(entry -> variant(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(InvestmentEvolutionSummaryView.VariantPerformanceView::sampleCount).reversed())
            .limit(8)
            .toList();
    }

    /**
     * 转换最近运行摘要。
     *
     * @param runs 闭环运行样本
     * @return 最近运行展示数据
     */
    private List<InvestmentEvolutionSummaryView.RecentRunView> recentRunViews(List<ClosedLoopRun> runs) {
        return runs.stream()
            .limit(8)
            .map(run -> InvestmentEvolutionSummaryView.RecentRunView.builder()
                .runBizId(run.bizId())
                .runNo(run.runNo())
                .runStatus(run.runStatus())
                .reportBizId(run.reportBizId())
                .portfolioBizId(run.portfolioBizId())
                .promptDisplay(versioned(run.promptCode(), run.promptVersion()))
                .qualityScore(scale(run.qualityScore()))
                .gateResult(run.gateResult())
                .startedAt(run.startedAt())
                .build())
            .toList();
    }

    /**
     * 生成顶部 KPI 卡片。
     *
     * @return 统一指标卡
     */
    private List<InvestmentEvolutionSummaryView.MetricView> kpis(
        InvestmentEvolutionSummaryView.ClosedLoopMetricsView closedLoop,
        InvestmentEvolutionSummaryView.PortfolioMetricsView portfolio,
        InvestmentEvolutionSummaryView.RiskMetricsView risk,
        InvestmentEvolutionSummaryView.ModelMetricsView model,
        InvestmentEvolutionSummaryView.FeedbackMetricsView feedback
    ) {
        return List.of(
            metric("closedLoopSuccessRate", "闭环成功率", percent(closedLoop.successRate()),
                statusByRate(closedLoop.successRate()), "最近闭环运行样本"),
            metric("latestReturnRate", "Mock收益率", percent(portfolio.latestReturnRate()),
                statusBySigned(portfolio.latestReturnRate()), "最近组合估值样本"),
            metric("maxDrawdown", "最大回撤", percent(portfolio.maxDrawdown()),
                drawdownStatus(portfolio.maxDrawdown()), "基于估值曲线计算"),
            metric("modelSuccessRate", "模型成功率", percent(model.successRate()),
                statusByRate(model.successRate()), "模型调用持久化审计"),
            metric("riskRejectCount", "风控拒绝", String.valueOf(risk.rejectCount()),
                risk.rejectCount() == 0 ? "GOOD" : "WARN", "最近风控检查样本"),
            metric("feedbackSamples", "反馈/回测样本", feedback.feedbackCount() + "/" + feedback.backtestCount(),
                feedback.feedbackCount() + feedback.backtestCount() >= MIN_TRUST_SAMPLE_SIZE ? "GOOD" : "UNKNOWN",
                "持续进化所需的结果归因样本")
        );
    }

    /**
     * 生成样本限制说明。
     *
     * @return 限制说明列表
     */
    private List<String> limitations(
        List<ClosedLoopRun> runs,
        List<InvestmentAnalysisReport> latestReports,
        List<AiModelCallAudit> audits,
        List<RiskCheck> risks,
        PortfolioWindow portfolioWindow
    ) {
        List<String> limitations = new ArrayList<>();
        if (runs.size() < MIN_TRUST_SAMPLE_SIZE) {
            limitations.add("闭环样本少于 " + MIN_TRUST_SAMPLE_SIZE + " 条，胜率和稳定性只能作为早期信号。");
        }
        if (latestReports.size() < MIN_TRUST_SAMPLE_SIZE) {
            limitations.add("投资报告样本不足，报告质量均值还不能代表长期质量。");
        }
        if (audits.size() < MIN_TRUST_SAMPLE_SIZE) {
            limitations.add("模型调用审计样本不足，A/B 归因只展示已观察到的候选变体。");
        }
        if (portfolioWindow.valuationPointCount() < 5) {
            limitations.add("Mock 组合估值点不足，收益、回撤和换手率代理指标仍需更多运行样本。");
        }
        if (risks.size() < MIN_TRUST_SAMPLE_SIZE) {
            limitations.add("风控样本不足，拒绝原因分布还不能代表真实风险面。");
        }
        return limitations;
    }

    /**
     * 归一化样本窗口大小。
     *
     * @param requestedSampleSize 请求样本量
     * @return 1-100 之间的样本量
     */
    private int sampleSize(Integer requestedSampleSize) {
        if (requestedSampleSize == null) {
            return MAX_SAMPLE_SIZE;
        }
        return Math.max(1, Math.min(MAX_SAMPLE_SIZE, requestedSampleSize));
    }

    /**
     * 判定样本状态。
     *
     * @return 样本状态
     */
    private String sampleStatus(int runCount, int auditCount, int valuationPointCount) {
        if (runCount >= MIN_TRUST_SAMPLE_SIZE && auditCount >= MIN_TRUST_SAMPLE_SIZE && valuationPointCount >= 10) {
            return "ENOUGH_FOR_TREND";
        }
        if (runCount > 0 || auditCount > 0 || valuationPointCount > 0) {
            return "EARLY_SIGNAL";
        }
        return "INSUFFICIENT_SAMPLE";
    }

    /**
     * 构造变体指标。
     *
     * @param variantKey 归因键
     * @param samples 该变体下的调用样本
     * @return 变体表现
     */
    private InvestmentEvolutionSummaryView.VariantPerformanceView variant(
        String variantKey,
        List<AiModelCallAudit> samples
    ) {
        AiModelCallAudit latest = samples.get(0);
        long success = count(samples, sample -> isAny(sample.callStatus(), "SUCCEEDED", "SUCCESS"));
        return InvestmentEvolutionSummaryView.VariantPerformanceView.builder()
            .variantKey(variantKey)
            .modelDisplay(versioned(latest.modelCode(), latest.modelVersion()))
            .promptDisplay(versioned(latest.promptCode(), latest.promptVersion()))
            .skillDisplay(versioned(latest.skillCode(), latest.skillVersion()))
            .sampleCount(samples.size())
            .successRate(rate(success, samples.size()))
            .averageDurationMs(average(samples.stream()
                .map(AiModelCallAudit::durationMs)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .toList()))
            .latestBusinessLabel(blankTo(latest.businessLabel(), latest.businessType()))
            .build();
    }

    /**
     * 生成 A/B 归因键。
     *
     * @param audit 模型调用审计
     * @return 稳定归因键
     */
    private String variantKey(AiModelCallAudit audit) {
        return String.join("|",
            "model=" + versioned(audit.modelCode(), audit.modelVersion()),
            "prompt=" + versioned(audit.promptCode(), audit.promptVersion()),
            "skill=" + versioned(audit.skillCode(), audit.skillVersion()),
            "scenario=" + blankTo(audit.scenarioCode(), "UNKNOWN")
        );
    }

    /**
     * 计算最大回撤。
     *
     * @param sortedValuations 按时间升序的估值快照
     * @return 最大回撤
     */
    private BigDecimal maxDrawdown(List<PortfolioValuation> sortedValuations) {
        BigDecimal peak = null;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        for (PortfolioValuation valuation : sortedValuations) {
            BigDecimal totalAsset = valuation.totalAsset();
            if (totalAsset == null || totalAsset.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (peak == null || totalAsset.compareTo(peak) > 0) {
                peak = totalAsset;
                continue;
            }
            BigDecimal drawdown = peak.subtract(totalAsset).divide(peak, 6, RoundingMode.HALF_UP);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }
        return scale(maxDrawdown);
    }

    /**
     * 计算平均值。
     *
     * @param values 数值集合
     * @return 平均值
     */
    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return scale(total.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP));
    }

    /**
     * 计算比例。
     *
     * @param numerator 分子
     * @param denominator 分母
     * @return 0-1 的比例值
     */
    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return null;
        }
        return scale(BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 6, RoundingMode.HALF_UP));
    }

    /**
     * 缩放小数。
     *
     * @param value 原始值
     * @return 保留四位小数的值
     */
    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 生成顶部指标卡。
     *
     * @param code 指标稳定编码
     * @param label 前端展示名称
     * @param value 已格式化展示值
     * @param status 指标状态，供前端决定颜色和风险感知
     * @param hint 指标口径说明
     * @return 分析页通用指标卡视图
     */
    private InvestmentEvolutionSummaryView.MetricView metric(
        String code,
        String label,
        String value,
        String status,
        String hint
    ) {
        return InvestmentEvolutionSummaryView.MetricView.builder()
            .code(code)
            .label(label)
            .value(value)
            .status(status)
            .hint(hint)
            .build();
    }

    /**
     * 按谓词统计样本数量。
     *
     * @param items 样本集合
     * @param predicate 命中条件
     * @param <T> 样本类型
     * @return 命中条件的样本数量
     */
    private <T> long count(List<T> items, java.util.function.Predicate<T> predicate) {
        return items.stream().filter(predicate).count();
    }

    /**
     * 将 0-1 的比例格式化成百分比文本。
     *
     * @param value 比例值
     * @return 百分比文本，空值返回短横线
     */
    private String percent(BigDecimal value) {
        return value == null ? "-" : value.multiply(ONE_HUNDRED).setScale(2, RoundingMode.HALF_UP) + "%";
    }

    /**
     * 根据成功率类比例生成健康状态。
     *
     * @param value 0-1 的比例值
     * @return GOOD/WARN/BAD/UNKNOWN
     */
    private String statusByRate(BigDecimal value) {
        if (value == null) {
            return "UNKNOWN";
        }
        if (value.compareTo(new BigDecimal("0.8")) >= 0) {
            return "GOOD";
        }
        return value.compareTo(new BigDecimal("0.5")) >= 0 ? "WARN" : "BAD";
    }

    /**
     * 根据收益正负生成组合表现状态。
     *
     * @param value 收益率或类似有符号指标
     * @return 非负为 GOOD，负值为 WARN，空值为 UNKNOWN
     */
    private String statusBySigned(BigDecimal value) {
        if (value == null) {
            return "UNKNOWN";
        }
        return value.compareTo(BigDecimal.ZERO) >= 0 ? "GOOD" : "WARN";
    }

    /**
     * 根据最大回撤生成风险状态。
     *
     * @param value 最大回撤，0-1 区间
     * @return 小于等于 10% 为 GOOD，否则 WARN，空值为 UNKNOWN
     */
    private String drawdownStatus(BigDecimal value) {
        if (value == null) {
            return "UNKNOWN";
        }
        return value.compareTo(new BigDecimal("0.1")) <= 0 ? "GOOD" : "WARN";
    }

    /**
     * 组合编码和版本展示文本。
     *
     * @param code 稳定编码
     * @param version 版本号
     * @return code@version 展示文本，编码为空时返回短横线
     */
    private String versioned(String code, String version) {
        String safeCode = blankTo(code, "-");
        return version == null || version.isBlank() ? safeCode : safeCode + "@" + version;
    }

    /**
     * 归一化空白文本。
     *
     * @param value 原始文本
     * @param fallback 空白时的兜底文本
     * @return 非空文本或兜底文本
     */
    private String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * 判断文本是否命中候选状态。
     *
     * @param value 待判断文本
     * @param candidates 候选状态集合
     * @return 忽略大小写命中时返回 true
     */
    private boolean isAny(String value, String... candidates) {
        if (value == null) {
            return false;
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        for (String candidate : candidates) {
            if (normalized.equals(candidate.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mock 组合统计窗口。
     *
     * @param portfolioBizIds 组合标识
     * @param valuations 估值样本
     * @param events 订单事件样本
     */
    private record PortfolioWindow(
        List<String> portfolioBizIds,
        List<PortfolioValuation> valuations,
        List<OrderEvent> events
    ) {
        int valuationPointCount() {
            return valuations.size();
        }
    }
}
