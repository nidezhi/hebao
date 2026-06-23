package com.example.dzcom.application.service.ai;

import com.alibaba.fastjson2.JSON;
import com.example.dzcom.application.command.ai.GenerateBacktestFromPortfolioCommand;
import com.example.dzcom.application.command.ai.SaveAiPromptEvaluationCommand;
import com.example.dzcom.application.command.ai.SaveBacktestResultCommand;
import com.example.dzcom.application.command.ai.SaveInvestmentFeedbackCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.BacktestResultView;
import com.example.dzcom.application.service.account.CurrentOperator;
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
import com.example.dzcom.domain.repository.portfolio.PortfolioSearchCriteria;
import com.example.dzcom.domain.repository.portfolio.PortfolioStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioValuationStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 投资闭环应用服务测试。 */
class InvestmentClosedLoopApplicationServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 23, 10, 0);

    /** 保存回测未显式传入状态时应落为待执行，避免前端非必要字段导致失败。 */
    @Test
    void shouldDefaultBacktestStatusToPending() {
        Fixture fixture = new Fixture();

        BacktestResultView result = fixture.service.saveBacktest(SaveBacktestResultCommand.builder()
            .strategyCode("trend-follow")
            .strategyVersion("v1")
            .startDate(LocalDate.of(2026, 6, 1))
            .endDate(LocalDate.of(2026, 6, 20))
            .initialCapital(new BigDecimal("100000"))
            .parameters("{\"window\":20}")
            .build());

        assertEquals("PENDING", result.status());
        assertEquals("user-1", result.ownerUserBizId());
    }

    /** 更新已有回测必须校验归属，不能通过 bizId 接管其他用户数据。 */
    @Test
    void shouldRejectBacktestUpdateWhenOwnerMismatch() {
        Fixture fixture = new Fixture();
        fixture.backtests.save(backtest("bt-other", "user-2"));

        assertThrows(BusinessException.class, () -> fixture.service.saveBacktest(SaveBacktestResultCommand.builder()
            .bizId("bt-other")
            .strategyCode("trend-follow")
            .strategyVersion("v1")
            .startDate(LocalDate.of(2026, 6, 1))
            .endDate(LocalDate.of(2026, 6, 20))
            .initialCapital(new BigDecimal("100000"))
            .parameters("{\"window\":20}")
            .status("SUCCEEDED")
            .build()));
    }

    /** 从模拟组合生成回测摘要必须校验组合归属。 */
    @Test
    void shouldRejectPortfolioBacktestWhenOwnerMismatch() {
        Fixture fixture = new Fixture();
        fixture.portfolios.save(portfolio("portfolio-other", "user-2"));

        assertThrows(BusinessException.class, () -> fixture.service.generateBacktestFromPortfolio(
            GenerateBacktestFromPortfolioCommand.builder()
                .portfolioBizId("portfolio-other")
                .strategyCode("mock-portfolio")
                .strategyVersion("v1")
                .build()));
    }

    /** 从模拟组合生成回测摘要时应保留前端传入的参数快照。 */
    @Test
    void shouldKeepClientParametersWhenGeneratingBacktestFromPortfolio() {
        Fixture fixture = new Fixture();
        fixture.portfolios.save(portfolio("portfolio-1", "user-1"));
        fixture.valuations.save(valuation("v1", "portfolio-1", NOW.minusDays(2), new BigDecimal("100000")));
        fixture.valuations.save(valuation("v2", "portfolio-1", NOW.minusDays(1), new BigDecimal("105000")));

        BacktestResultView result = fixture.service.generateBacktestFromPortfolio(
            GenerateBacktestFromPortfolioCommand.builder()
                .portfolioBizId("portfolio-1")
                .strategyCode("mock-portfolio")
                .strategyVersion("v1")
                .parameters("{\"rebalance\":\"monthly\"}")
                .build());

        var parameters = JSON.parseObject(result.parameters());
        assertEquals("MOCK_PORTFOLIO_VALUATION", parameters.getString("source"));
        assertEquals("monthly", parameters.getJSONObject("clientParameters").getString("rebalance"));
    }

    /** 回测详情必须只允许所有者查看。 */
    @Test
    void shouldRejectBacktestDetailWhenOwnerMismatch() {
        Fixture fixture = new Fixture();
        fixture.backtests.save(backtest("bt-other", "user-2"));

        assertThrows(BusinessException.class, () -> fixture.service.backtestDetail("bt-other"));
    }

    /** 反馈详情必须只允许反馈用户查看。 */
    @Test
    void shouldRejectFeedbackDetailWhenOwnerMismatch() {
        Fixture fixture = new Fixture();
        fixture.feedbacks.save(feedback("fb-other", "user-2", null));

        assertThrows(BusinessException.class, () -> fixture.service.feedbackDetail("fb-other"));
    }

    /** 保存反馈时不能关联其他用户的回测结果。 */
    @Test
    void shouldRejectFeedbackWithForeignBacktest() {
        Fixture fixture = new Fixture();
        fixture.backtests.save(backtest("bt-other", "user-2"));

        assertThrows(BusinessException.class, () -> fixture.service.saveFeedback(SaveInvestmentFeedbackCommand.builder()
            .targetType("BACKTEST")
            .targetBizId("bt-other")
            .backtestBizId("bt-other")
            .feedbackAction("REJECT")
            .build()));
    }

    /** Prompt 评估保存时不能关联当前用户不可见的反馈。 */
    @Test
    void shouldRejectPromptEvaluationWithForeignFeedback() {
        Fixture fixture = new Fixture();
        fixture.feedbacks.save(feedback("fb-other", "user-2", null));

        assertThrows(BusinessException.class, () -> fixture.service.savePromptEvaluation(
            SaveAiPromptEvaluationCommand.builder()
                .promptCode("INVEST_PLAN")
                .promptVersion("v1")
                .scenario("FEEDBACK_LOOP")
                .feedbackBizId("fb-other")
                .score(new BigDecimal("0.8"))
                .build()));
    }

    /** Prompt 评估详情必须由评估人或关联数据所有者查看。 */
    @Test
    void shouldRejectPromptEvaluationDetailWhenNotVisible() {
        Fixture fixture = new Fixture();
        fixture.evaluations.save(AiPromptEvaluation.builder()
            .bizId("eval-other")
            .promptCode("INVEST_PLAN")
            .promptVersion("v1")
            .scenario("FEEDBACK_LOOP")
            .score(new BigDecimal("0.8"))
            .reviewStatus("PENDING")
            .evaluatorType("SYSTEM")
            .evaluatorBizId("user-2")
            .evaluatedAt(NOW)
            .createdAt(NOW)
            .build());

        assertThrows(BusinessException.class, () -> fixture.service.promptEvaluationDetail("eval-other"));
    }

    private static BacktestResult backtest(String bizId, String ownerUserBizId) {
        return BacktestResult.builder()
            .bizId(bizId)
            .ownerUserBizId(ownerUserBizId)
            .strategyCode("MOCK")
            .strategyVersion("v1")
            .startDate(LocalDate.of(2026, 6, 1))
            .endDate(LocalDate.of(2026, 6, 20))
            .initialCapital(new BigDecimal("100000"))
            .parameters("{}")
            .status("SUCCEEDED")
            .createdAt(NOW)
            .updatedAt(NOW)
            .build();
    }

    private static InvestmentFeedback feedback(String bizId, String userBizId, String backtestBizId) {
        return InvestmentFeedback.builder()
            .bizId(bizId)
            .userBizId(userBizId)
            .targetType("BACKTEST")
            .targetBizId(backtestBizId == null ? "target-1" : backtestBizId)
            .backtestBizId(backtestBizId)
            .feedbackAction("WATCH")
            .createdAt(NOW)
            .build();
    }

    private static Portfolio portfolio(String bizId, String ownerUserBizId) {
        return Portfolio.builder()
            .bizId(bizId)
            .portfolioNo("PF-" + bizId)
            .ownerUserBizId(ownerUserBizId)
            .portfolioName("模拟组合")
            .portfolioType("SIMULATION")
            .baseCurrency("CNY")
            .status(1)
            .version(0)
            .createdAt(NOW)
            .updatedAt(NOW)
            .createdBy(ownerUserBizId)
            .updatedBy(ownerUserBizId)
            .deleted(0)
            .build();
    }

    private static PortfolioValuation valuation(
        String bizId,
        String portfolioBizId,
        LocalDateTime valuationTime,
        BigDecimal totalAsset
    ) {
        return PortfolioValuation.builder()
            .bizId(bizId)
            .portfolioBizId(portfolioBizId)
            .valuationTime(valuationTime)
            .baseCurrency("CNY")
            .totalAsset(totalAsset)
            .cashBalance(totalAsset)
            .positionValue(BigDecimal.ZERO)
            .totalCost(totalAsset)
            .unrealizedProfit(BigDecimal.ZERO)
            .realizedProfit(BigDecimal.ZERO)
            .totalReturnRate(BigDecimal.ZERO)
            .sourceCode("TEST")
            .createdAt(valuationTime)
            .build();
    }

    /** 测试夹具，集中提供闭环服务和内存仓储。 */
    private static final class Fixture {
        private final InMemoryBacktestStore backtests = new InMemoryBacktestStore();
        private final InMemoryFeedbackStore feedbacks = new InMemoryFeedbackStore();
        private final InMemoryEvaluationStore evaluations = new InMemoryEvaluationStore();
        private final InMemoryPortfolioStore portfolios = new InMemoryPortfolioStore();
        private final InMemoryValuationStore valuations = new InMemoryValuationStore();
        private final InvestmentClosedLoopApplicationService service = new InvestmentClosedLoopApplicationService(
            backtests,
            feedbacks,
            evaluations,
            portfolios,
            valuations,
            () -> new CurrentOperator("user-1", "token", Set.of(), Set.of()),
            new IncrementalIdGenerator(),
            () -> NOW
        );
    }

    /** 内存回测仓储。 */
    private static final class InMemoryBacktestStore implements BacktestResultStore {
        private final Map<String, BacktestResult> results = new HashMap<>();

        @Override
        public BacktestResult save(BacktestResult result) {
            results.put(result.bizId(), result);
            return result;
        }

        @Override
        public Optional<BacktestResult> findByBizId(String bizId) {
            return Optional.ofNullable(results.get(bizId));
        }

        @Override
        public PageResult<BacktestResult> search(BacktestResultSearchCriteria criteria) {
            List<BacktestResult> items = results.values().stream()
                .filter(result -> criteria.ownerUserBizId().equals(result.ownerUserBizId()))
                .toList();
            return page(items, criteria.page(), criteria.size());
        }
    }

    /** 内存反馈仓储。 */
    private static final class InMemoryFeedbackStore implements InvestmentFeedbackStore {
        private final Map<String, InvestmentFeedback> feedbacks = new HashMap<>();

        @Override
        public InvestmentFeedback save(InvestmentFeedback feedback) {
            feedbacks.put(feedback.bizId(), feedback);
            return feedback;
        }

        @Override
        public Optional<InvestmentFeedback> findByBizId(String bizId) {
            return Optional.ofNullable(feedbacks.get(bizId));
        }

        @Override
        public PageResult<InvestmentFeedback> search(InvestmentFeedbackSearchCriteria criteria) {
            List<InvestmentFeedback> items = feedbacks.values().stream()
                .filter(feedback -> criteria.userBizId().equals(feedback.userBizId()))
                .toList();
            return page(items, criteria.page(), criteria.size());
        }
    }

    /** 内存 Prompt 评估仓储。 */
    private static final class InMemoryEvaluationStore implements AiPromptEvaluationStore {
        private final Map<String, AiPromptEvaluation> evaluations = new HashMap<>();

        @Override
        public AiPromptEvaluation save(AiPromptEvaluation evaluation) {
            evaluations.put(evaluation.bizId(), evaluation);
            return evaluation;
        }

        @Override
        public Optional<AiPromptEvaluation> findByBizId(String bizId) {
            return Optional.ofNullable(evaluations.get(bizId));
        }

        @Override
        public PageResult<AiPromptEvaluation> search(AiPromptEvaluationSearchCriteria criteria) {
            List<AiPromptEvaluation> items = evaluations.values().stream()
                .filter(evaluation -> criteria.visibleUserBizId().equals(evaluation.evaluatorBizId()))
                .toList();
            return page(items, criteria.page(), criteria.size());
        }
    }

    /** 内存组合仓储。 */
    private static final class InMemoryPortfolioStore implements PortfolioStore {
        private final Map<String, Portfolio> portfolios = new HashMap<>();

        @Override
        public Portfolio save(Portfolio portfolio) {
            portfolios.put(portfolio.bizId(), portfolio);
            return portfolio;
        }

        @Override
        public Optional<Portfolio> findByBizId(String bizId) {
            return Optional.ofNullable(portfolios.get(bizId));
        }

        @Override
        public PageResult<Portfolio> search(PortfolioSearchCriteria criteria) {
            List<Portfolio> items = portfolios.values().stream()
                .filter(portfolio -> criteria.ownerUserBizId().equals(portfolio.ownerUserBizId()))
                .toList();
            return page(items, criteria.page(), criteria.size());
        }
    }

    /** 内存估值仓储。 */
    private static final class InMemoryValuationStore implements PortfolioValuationStore {
        private final List<PortfolioValuation> valuations = new ArrayList<>();

        @Override
        public PortfolioValuation save(PortfolioValuation valuation) {
            valuations.add(valuation);
            return valuation;
        }

        @Override
        public Optional<PortfolioValuation> findLatestByPortfolioBizId(String portfolioBizId) {
            return valuations.stream()
                .filter(valuation -> portfolioBizId.equals(valuation.portfolioBizId()))
                .reduce((first, second) -> second);
        }

        @Override
        public Optional<PortfolioValuation> findFirstByPortfolioBizId(String portfolioBizId) {
            return valuations.stream()
                .filter(valuation -> portfolioBizId.equals(valuation.portfolioBizId()))
                .findFirst();
        }

        @Override
        public List<PortfolioValuation> findHistoryByPortfolioBizId(String portfolioBizId, int limit) {
            return valuations.stream()
                .filter(valuation -> portfolioBizId.equals(valuation.portfolioBizId()))
                .limit(limit)
                .toList();
        }
    }

    /** 递增业务 ID 生成器。 */
    private static final class IncrementalIdGenerator implements IdGenerator {
        private int sequence;

        @Override
        public String newBizId() {
            sequence++;
            return "biz-" + sequence;
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }

    private static <T> PageResult<T> page(List<T> items, int page, int size) {
        assertTrue(page > 0);
        assertTrue(size > 0);
        return PageResult.<T>builder()
            .items(items)
            .total(items.size())
            .page(page)
            .size(size)
            .totalPages((int) Math.ceil((double) items.size() / size))
            .build();
    }
}
