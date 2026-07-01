package com.example.dzcom.application.service.analytics;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.dto.analytics.InvestmentEvolutionSummaryView;
import com.example.dzcom.domain.model.ai.AiModelCallAudit;
import com.example.dzcom.domain.model.ai.BacktestResult;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.ai.InvestmentFeedback;
import com.example.dzcom.domain.model.portfolio.OrderEvent;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.model.risk.RiskCheck;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.ClosedLoopStep;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditStore;
import com.example.dzcom.domain.repository.ai.BacktestResultSearchCriteria;
import com.example.dzcom.domain.repository.ai.BacktestResultStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackStore;
import com.example.dzcom.domain.repository.portfolio.OrderEventStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioValuationStore;
import com.example.dzcom.domain.repository.risk.RiskCheckSearchCriteria;
import com.example.dzcom.domain.repository.risk.RiskCheckStore;
import com.example.dzcom.domain.repository.task.ClosedLoopRunSearchCriteria;
import com.example.dzcom.domain.repository.task.ClosedLoopRunStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 投资闭环持续进化分析应用服务测试。 */
class InvestmentEvolutionAnalyticsApplicationServiceTest {

    @Test
    void shouldAggregateEvolutionMetricsAndVariants() {
        Fixture fixture = new Fixture();
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        fixture.closedLoops.runs.add(ClosedLoopRun.builder()
            .bizId("run-1")
            .runNo("CLR-1")
            .runStatus("SUCCEEDED")
            .gateResult("PASS")
            .reportBizId("report-1")
            .portfolioBizId("portfolio-1")
            .promptCode("auto-report")
            .promptVersion("v1")
            .qualityScore(new BigDecimal("0.82"))
            .startedAt(now.minusMinutes(10))
            .build());
        fixture.closedLoops.runs.add(ClosedLoopRun.builder()
            .bizId("run-2")
            .runNo("CLR-2")
            .runStatus("FAILED")
            .gateResult("BLOCK")
            .portfolioBizId("portfolio-1")
            .promptCode("auto-report")
            .promptVersion("v2")
            .qualityScore(new BigDecimal("0.41"))
            .startedAt(now.minusMinutes(20))
            .build());
        fixture.reports.reports.add(InvestmentAnalysisReport.builder()
            .bizId("report-1")
            .status("SUCCEEDED")
            .dataQualityScore(new BigDecimal("0.82"))
            .generatedAt(now.minusMinutes(9))
            .build());
        fixture.valuations.items.add(valuation("valuation-1", "portfolio-1", new BigDecimal("100000"), now.minusMinutes(30)));
        fixture.valuations.items.add(valuation("valuation-2", "portfolio-1", new BigDecimal("103000"), now.minusMinutes(5)));
        fixture.orderEvents.items.add(OrderEvent.builder()
            .bizId("event-1")
            .orderBizId("order-1")
            .eventType("FILLED")
            .toStatus("FILLED")
            .occurredAt(now.minusMinutes(4))
            .build());
        fixture.risks.items.add(RiskCheck.builder()
            .bizId("risk-1")
            .checkResult("REJECT")
            .reasonCode("CASH_LIMIT")
            .checkedAt(now.minusMinutes(3))
            .build());
        fixture.audits.items.add(AiModelCallAudit.builder()
            .bizId("audit-1")
            .callId("call-1")
            .callStatus("SUCCEEDED")
            .modelCode("qwen")
            .modelVersion("v1")
            .promptCode("auto-report")
            .promptVersion("v1")
            .skillCode("report-skill")
            .skillVersion("v1")
            .scenarioCode("INVESTMENT_REPORT")
            .durationMs(1000L)
            .businessLabel("报告1")
            .createdAt(now.minusMinutes(2))
            .build());
        fixture.audits.items.add(AiModelCallAudit.builder()
            .bizId("audit-2")
            .callId("call-2")
            .callStatus("FAILED")
            .modelCode("qwen")
            .modelVersion("v1")
            .promptCode("auto-report")
            .promptVersion("v2")
            .skillCode("report-skill")
            .skillVersion("v1")
            .scenarioCode("INVESTMENT_REPORT")
            .durationMs(2000L)
            .businessLabel("报告2")
            .createdAt(now.minusMinutes(1))
            .build());
        fixture.feedbacks.items.add(InvestmentFeedback.builder()
            .bizId("feedback-1")
            .feedbackAction("ACCEPT")
            .createdAt(now)
            .build());
        fixture.backtests.items.add(BacktestResult.builder()
            .bizId("backtest-1")
            .status("COMPLETED")
            .createdAt(now)
            .build());

        InvestmentEvolutionSummaryView summary = fixture.service.summary(20);

        assertEquals("EARLY_SIGNAL", summary.sampleStatus());
        assertEquals(2, summary.closedLoop().sampleCount());
        assertEquals(new BigDecimal("0.5000"), summary.closedLoop().successRate());
        assertEquals(new BigDecimal("0.0300"), summary.portfolio().latestReturnRate());
        assertEquals(1, summary.risk().rejectCount());
        assertEquals(new BigDecimal("0.5000"), summary.model().successRate());
        assertEquals(2, summary.variants().size());
        assertFalse(summary.limitations().isEmpty());
        assertNotNull(summary.kpis());
        assertTrue(summary.recentRuns().stream().anyMatch(run -> "CLR-1".equals(run.runNo())));
    }

    private static PortfolioValuation valuation(String bizId, String portfolioBizId, BigDecimal asset, LocalDateTime time) {
        return PortfolioValuation.builder()
            .bizId(bizId)
            .portfolioBizId(portfolioBizId)
            .valuationTime(time)
            .totalAsset(asset)
            .totalReturnRate(asset.subtract(new BigDecimal("100000")).divide(new BigDecimal("100000")))
            .build();
    }

    /** 测试依赖集合。 */
    private static final class Fixture {
        private final MemoryClosedLoopRunStore closedLoops = new MemoryClosedLoopRunStore();
        private final MemoryReportStore reports = new MemoryReportStore();
        private final MemoryValuationStore valuations = new MemoryValuationStore();
        private final MemoryOrderEventStore orderEvents = new MemoryOrderEventStore();
        private final MemoryRiskCheckStore risks = new MemoryRiskCheckStore();
        private final MemoryAuditStore audits = new MemoryAuditStore();
        private final MemoryFeedbackStore feedbacks = new MemoryFeedbackStore();
        private final MemoryBacktestStore backtests = new MemoryBacktestStore();
        private final InvestmentEvolutionAnalyticsApplicationService service =
            new InvestmentEvolutionAnalyticsApplicationService(
                closedLoops,
                reports,
                valuations,
                orderEvents,
                risks,
                audits,
                feedbacks,
                backtests,
                () -> LocalDateTime.of(2026, 7, 1, 10, 0)
            );
    }

    /** 内存闭环仓储。 */
    private static final class MemoryClosedLoopRunStore implements ClosedLoopRunStore {
        private final List<ClosedLoopRun> runs = new ArrayList<>();

        @Override
        public ClosedLoopRun saveRun(ClosedLoopRun run) {
            runs.add(run);
            return run;
        }

        @Override
        public ClosedLoopStep saveStep(ClosedLoopStep step) {
            return step;
        }

        @Override
        public Optional<ClosedLoopRun> findRunByBizId(String bizId) {
            return runs.stream().filter(run -> run.bizId().equals(bizId)).findFirst();
        }

        @Override
        public List<ClosedLoopStep> findStepsByRunBizId(String runBizId) {
            return List.of();
        }

        @Override
        public PageResult<ClosedLoopRun> searchRuns(ClosedLoopRunSearchCriteria criteria) {
            List<ClosedLoopRun> items = runs.stream()
                .sorted(Comparator.comparing(ClosedLoopRun::startedAt).reversed())
                .limit(criteria.size())
                .toList();
            return page(items, criteria.size());
        }
    }

    /** 内存报告仓储。 */
    private static final class MemoryReportStore implements InvestmentAnalysisReportStore {
        private final List<InvestmentAnalysisReport> reports = new ArrayList<>();

        @Override
        public InvestmentAnalysisReport save(InvestmentAnalysisReport report) {
            reports.add(report);
            return report;
        }

        @Override
        public Optional<InvestmentAnalysisReport> findByBizId(String bizId) {
            return reports.stream().filter(report -> report.bizId().equals(bizId)).findFirst();
        }

        @Override
        public PageResult<InvestmentAnalysisReport> search(InvestmentAnalysisReportSearchCriteria criteria) {
            return page(reports, criteria.size());
        }

        @Override
        public PageResult<InvestmentAnalysisReport> latest(int size) {
            return page(reports.stream().limit(size).toList(), size);
        }
    }

    /** 内存估值仓储。 */
    private static final class MemoryValuationStore implements PortfolioValuationStore {
        private final List<PortfolioValuation> items = new ArrayList<>();

        @Override
        public PortfolioValuation save(PortfolioValuation valuation) {
            items.add(valuation);
            return valuation;
        }

        @Override
        public Optional<PortfolioValuation> findLatestByPortfolioBizId(String portfolioBizId) {
            return items.stream().filter(item -> portfolioBizId.equals(item.portfolioBizId()))
                .max(Comparator.comparing(PortfolioValuation::valuationTime));
        }

        @Override
        public Optional<PortfolioValuation> findFirstByPortfolioBizId(String portfolioBizId) {
            return items.stream().filter(item -> portfolioBizId.equals(item.portfolioBizId()))
                .min(Comparator.comparing(PortfolioValuation::valuationTime));
        }

        @Override
        public List<PortfolioValuation> findHistoryByPortfolioBizId(String portfolioBizId, int limit) {
            return items.stream().filter(item -> portfolioBizId.equals(item.portfolioBizId()))
                .sorted(Comparator.comparing(PortfolioValuation::valuationTime))
                .limit(limit)
                .toList();
        }
    }

    /** 内存订单事件仓储。 */
    private static final class MemoryOrderEventStore implements OrderEventStore {
        private final List<OrderEvent> items = new ArrayList<>();

        @Override
        public OrderEvent save(OrderEvent event) {
            items.add(event);
            return event;
        }

        @Override
        public List<OrderEvent> findByOrderBizId(String orderBizId) {
            return items.stream().filter(item -> orderBizId.equals(item.orderBizId())).toList();
        }

        @Override
        public List<OrderEvent> findRecentByPortfolioBizId(String portfolioBizId, int limit) {
            return items.stream()
                .limit(limit)
                .toList();
        }
    }

    /** 内存风控仓储。 */
    private static final class MemoryRiskCheckStore implements RiskCheckStore {
        private final List<RiskCheck> items = new ArrayList<>();

        @Override
        public RiskCheck save(RiskCheck riskCheck) {
            items.add(riskCheck);
            return riskCheck;
        }

        @Override
        public PageResult<RiskCheck> search(RiskCheckSearchCriteria criteria) {
            return page(items.stream().limit(criteria.size()).toList(), criteria.size());
        }
    }

    /** 内存模型调用审计仓储。 */
    private static final class MemoryAuditStore implements AiModelCallAuditStore {
        private final List<AiModelCallAudit> items = new ArrayList<>();

        @Override
        public AiModelCallAudit save(AiModelCallAudit audit) {
            items.add(audit);
            return audit;
        }

        @Override
        public Optional<AiModelCallAudit> findByBizId(String bizId) {
            return items.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public Optional<AiModelCallAudit> findByCallId(String callId) {
            return items.stream().filter(item -> item.callId().equals(callId)).findFirst();
        }

        @Override
        public PageResult<AiModelCallAudit> search(AiModelCallAuditSearchCriteria criteria) {
            return page(items.stream()
                .sorted(Comparator.comparing(AiModelCallAudit::createdAt).reversed())
                .limit(criteria.size())
                .toList(), criteria.size());
        }
    }

    /** 内存反馈仓储。 */
    private static final class MemoryFeedbackStore implements InvestmentFeedbackStore {
        private final List<InvestmentFeedback> items = new ArrayList<>();

        @Override
        public InvestmentFeedback save(InvestmentFeedback feedback) {
            items.add(feedback);
            return feedback;
        }

        @Override
        public Optional<InvestmentFeedback> findByBizId(String bizId) {
            return items.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public PageResult<InvestmentFeedback> search(InvestmentFeedbackSearchCriteria criteria) {
            return page(items.stream().limit(criteria.size()).toList(), criteria.size());
        }
    }

    /** 内存回测仓储。 */
    private static final class MemoryBacktestStore implements BacktestResultStore {
        private final List<BacktestResult> items = new ArrayList<>();

        @Override
        public BacktestResult save(BacktestResult result) {
            items.add(result);
            return result;
        }

        @Override
        public Optional<BacktestResult> findByBizId(String bizId) {
            return items.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public PageResult<BacktestResult> search(BacktestResultSearchCriteria criteria) {
            return page(items.stream().limit(criteria.size()).toList(), criteria.size());
        }
    }

    private static <T> PageResult<T> page(List<T> items, int size) {
        return PageResult.<T>builder()
            .items(items)
            .total(items.size())
            .page(1)
            .size(size)
            .totalPages(items.isEmpty() ? 0 : 1)
            .build();
    }
}
