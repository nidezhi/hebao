package com.example.dzcom.application.service.portfolio;

import com.example.dzcom.application.command.portfolio.CancelMockOrderCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockBuyCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockRebalanceCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockSellCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.risk.RiskAuditApplicationService;
import com.example.dzcom.domain.repository.risk.RiskCheckSearchCriteria;
import com.example.dzcom.domain.repository.risk.RiskCheckStore;
import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.portfolio.MockOrder;
import com.example.dzcom.domain.model.portfolio.OrderEvent;
import com.example.dzcom.domain.model.portfolio.Portfolio;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.model.portfolio.Position;
import com.example.dzcom.domain.model.portfolio.TradeExecution;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductInvestmentProfile;
import com.example.dzcom.domain.model.product.ProductThemeRelation;
import com.example.dzcom.domain.model.task.ThemeProductPerformance;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.portfolio.MockOrderStore;
import com.example.dzcom.domain.repository.portfolio.OrderEventStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioSearchCriteria;
import com.example.dzcom.domain.repository.portfolio.PortfolioStore;
import com.example.dzcom.domain.repository.portfolio.PortfolioValuationStore;
import com.example.dzcom.domain.repository.portfolio.PositionStore;
import com.example.dzcom.domain.repository.portfolio.TradeExecutionStore;
import com.example.dzcom.domain.repository.product.ProductInvestmentProfileStore;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.product.ProductThemeRelationStore;
import com.example.dzcom.application.assembler.portfolio.MockOrderExecutionViewAssembler;
import com.example.dzcom.application.assembler.portfolio.MockPortfolioViewAssembler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

/** 模拟组合交易应用服务测试。 */
class MockPortfolioApplicationServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 23, 10, 0);

    /** 卖出应按平均成本扣减持仓、增加现金并追加订单事件。 */
    @Test
    void shouldSellPositionAndAppendOrderEvent() {
        Fixture fixture = new Fixture();
        fixture.positions.save(position(new BigDecimal("8000"), new BigDecimal("10000")));

        fixture.service.sell(ExecuteMockSellCommand.builder()
            .portfolioBizId("portfolio-1")
            .productBizId("product-1")
            .quantity(new BigDecimal("4000"))
            .idempotencyKey("sell-1")
            .build());

        Position saved = fixture.positions.findByDimension("portfolio-1", "product-1", "LONG").orElseThrow();
        assertEquals(0, new BigDecimal("4000.00000000").compareTo(saved.quantity()));
        assertEquals(0, new BigDecimal("5000.00000000").compareTo(saved.costAmount()));
        assertEquals(0, new BigDecimal("0.48755323").compareTo(fixture.valuations.latest.realizedProfit()));
        assertEquals("MOCK_SELL_FILLED", fixture.valuations.latest.sourceCode());
        assertEquals(1, fixture.orderEvents.events.size());
        assertEquals("FILLED", fixture.orderEvents.events.get(0).eventType());
    }

    /** 已成交终态订单必须拒绝撤单。 */
    @Test
    void shouldRejectCancelForTerminalOrder() {
        Fixture fixture = new Fixture();
        MockOrder order = mockOrder("order-filled", "FILLED");
        fixture.orders.save(order);

        assertThrows(BusinessException.class, () -> fixture.service.cancelOrder(
            CancelMockOrderCommand.builder()
                .orderBizId(order.bizId())
                .cancelReason("late cancel")
                .build()));
        assertTrue(fixture.orderEvents.events.isEmpty());
    }

    /** 非终态订单可以撤销并写入可查询事件。 */
    @Test
    void shouldCancelOpenOrderAndExposeEvent() {
        Fixture fixture = new Fixture();
        MockOrder order = mockOrder("order-open", "SUBMITTED");
        fixture.orders.save(order);

        fixture.service.cancelOrder(CancelMockOrderCommand.builder()
            .orderBizId(order.bizId())
            .cancelReason("用户撤销")
            .build());

        assertEquals("CANCELLED", fixture.orders.findByBizId(order.bizId()).orElseThrow().status());
        assertEquals(1, fixture.service.orderEvents(order.bizId()).size());
        assertEquals("CANCELLED", fixture.service.orderEvents(order.bizId()).get(0).eventType());
    }

    /** 再平衡目标权重超过 1 必须拒绝，避免生成无效交易计划。 */
    @Test
    void shouldRejectRebalanceWhenTargetWeightExceedsOne() {
        Fixture fixture = new Fixture();

        assertThrows(BusinessException.class, () -> fixture.service.rebalance(
            ExecuteMockRebalanceCommand.builder()
                .portfolioBizId("portfolio-1")
                .targets(List.of(
                    ExecuteMockRebalanceCommand.TargetWeight.builder()
                        .productBizId("product-1")
                        .targetWeight(new BigDecimal("0.8"))
                        .build(),
                    ExecuteMockRebalanceCommand.TargetWeight.builder()
                        .productBizId("product-2")
                        .targetWeight(new BigDecimal("0.4"))
                        .build()
                ))
                .build()));
        assertTrue(fixture.riskStore.saved.stream()
            .anyMatch(check -> "TARGET_WEIGHT_EXCEEDED".equals(check.reasonCode())));
    }

    /** 模拟买入成功时也要沉淀 PASS 风控样本，支撑风控审计页闭环。 */
    @Test
    void shouldRecordPassRiskChecksWhenBuySucceeds() {
        Fixture fixture = new Fixture();

        fixture.service.buy(ExecuteMockBuyCommand.builder()
            .portfolioBizId("portfolio-1")
            .productBizId("product-1")
            .amount(new BigDecimal("1000"))
            .idempotencyKey("buy-pass-risk")
            .build());

        assertTrue(fixture.riskStore.saved.stream()
            .anyMatch(check -> "PASS".equals(check.checkResult())
                && "PRODUCT_MOCK_TRADABLE".equals(check.reasonCode())));
    }

    /** 市场级报告没有主题关系时，应能从可 Mock 产品池选择合格产品继续闭环。 */
    @Test
    void shouldBuyFromMarketLevelReportBySelectingMockTradableProduct() {
        Fixture fixture = new Fixture();
        fixture.reports.save(InvestmentAnalysisReport.builder()
            .bizId("report-market")
            .requestId("request-market")
            .providerCode("OPENAI_COMPATIBLE")
            .modelCode("openai-compatible-analysis")
            .marketScope("CN_MAINLAND")
            .status("SUCCEEDED")
            .confidenceLevel("MEDIUM_CONFIDENCE")
            .dataQualityScore(new BigDecimal("0.80"))
            .dataQualityGate("{\"passed\":true}")
            .investmentPlan("{\"planType\":\"REFERENCE_ALLOCATION\",\"referenceAllocationAmount\":1000}")
            .generatedAt(NOW)
            .createdAt(NOW)
            .build());

        var execution = fixture.service.buyFromReport(ExecuteMockPlanFromReportCommand.builder()
            .portfolioBizId("portfolio-1")
            .reportBizId("report-market")
            .idempotencyKey("market-report-buy")
            .build());

        assertEquals("product-1", execution.order().productBizId());
        assertEquals("FILLED", execution.order().status());
    }

    /** 构建测试用模拟持仓。 */
    private static Position position(BigDecimal quantity, BigDecimal costAmount) {
        return Position.builder()
            .bizId("position-1")
            .portfolioBizId("portfolio-1")
            .productBizId("product-1")
            .positionSide("LONG")
            .quantity(quantity)
            .availableQuantity(quantity)
            .averageCost(costAmount.divide(quantity, 8, java.math.RoundingMode.HALF_UP))
            .costAmount(costAmount)
            .realizedProfit(BigDecimal.ZERO)
            .lastTradeAt(NOW.minusDays(1))
            .version(0)
            .createdAt(NOW.minusDays(1))
            .updatedAt(NOW.minusDays(1))
            .deleted(0)
            .build();
    }

    /** 构建测试用模拟订单。 */
    private static MockOrder mockOrder(String bizId, String status) {
        return MockOrder.builder()
            .bizId(bizId)
            .orderNo("MO-" + bizId)
            .idempotencyKey("idem-" + bizId)
            .userBizId("user-1")
            .portfolioBizId("portfolio-1")
            .productBizId("product-1")
            .channelCode("SIMULATOR")
            .orderSide("BUY")
            .orderType("AMOUNT")
            .currency("CNY")
            .requestedAmount(new BigDecimal("100"))
            .executedQuantity(BigDecimal.ZERO)
            .executedAmount(BigDecimal.ZERO)
            .feeAmount(BigDecimal.ZERO)
            .status(status)
            .createdAt(NOW)
            .updatedAt(NOW)
            .createdBy("user-1")
            .deleted(0)
            .build();
    }

    /** 测试夹具，集中提供应用服务和内存仓储。 */
    private static final class Fixture {
        private final InMemoryPortfolioStore portfolios = new InMemoryPortfolioStore();
        private final InMemoryPositionStore positions = new InMemoryPositionStore();
        private final InMemoryValuationStore valuations = new InMemoryValuationStore();
        private final InMemoryOrderStore orders = new InMemoryOrderStore();
        private final InMemoryOrderEventStore orderEvents = new InMemoryOrderEventStore();
        private final InMemoryExecutionStore executions = new InMemoryExecutionStore();
        private final InMemoryAnalysisReportStore reports = new InMemoryAnalysisReportStore();
        private final InMemoryRiskCheckStore riskStore = new InMemoryRiskCheckStore();
        private final RiskAuditApplicationService riskAudits = new RiskAuditApplicationService(
            riskStore,
            new IncrementalIdGenerator(),
            () -> NOW
        );
        private final MockPortfolioApplicationService service;

        private Fixture() {
            service = new MockPortfolioApplicationService(
                portfolios,
                positions,
                valuations,
                orders,
                orderEvents,
                executions,
                new InMemoryProductStore(),
                new FixedProfileStore(),
                new EmptyThemeRelationStore(),
                reports,
                new FixedQuoteStore(),
                new MockPortfolioViewAssembler(),
                new MockOrderExecutionViewAssembler(),
                () -> new CurrentOperator("user-1", "token", Set.of(), Set.of()),
                riskAudits,
                new IncrementalIdGenerator(),
                () -> NOW
            );
        }
    }

    /** 内存风险检查仓储。 */
    private static final class InMemoryRiskCheckStore implements RiskCheckStore {
        private final List<com.example.dzcom.domain.model.risk.RiskCheck> saved = new ArrayList<>();

        @Override
        public com.example.dzcom.domain.model.risk.RiskCheck save(
            com.example.dzcom.domain.model.risk.RiskCheck riskCheck
        ) {
            saved.add(riskCheck);
            return riskCheck;
        }

        @Override
        public PageResult<com.example.dzcom.domain.model.risk.RiskCheck> search(
            RiskCheckSearchCriteria criteria
        ) {
            throw new UnsupportedOperationException();
        }
    }

    /** 内存组合仓储。 */
    private static final class InMemoryPortfolioStore implements PortfolioStore {
        private final Portfolio portfolio = Portfolio.builder()
            .bizId("portfolio-1")
            .portfolioNo("MP1")
            .ownerUserBizId("user-1")
            .portfolioName("模拟组合")
            .portfolioType("SIMULATION")
            .baseCurrency("CNY")
            .status(1)
            .createdAt(NOW)
            .updatedAt(NOW)
            .createdBy("user-1")
            .updatedBy("user-1")
            .deleted(0)
            .build();

        @Override
        public Portfolio save(Portfolio value) {
            return value;
        }

        @Override
        public Optional<Portfolio> findByBizId(String bizId) {
            return portfolio.bizId().equals(bizId) ? Optional.of(portfolio) : Optional.empty();
        }

        @Override
        public PageResult<Portfolio> search(PortfolioSearchCriteria criteria) {
            throw new UnsupportedOperationException();
        }
    }

    /** 内存持仓仓储。 */
    private static final class InMemoryPositionStore implements PositionStore {
        private final Map<String, Position> positions = new HashMap<>();

        @Override
        public Position save(Position position) {
            positions.put(position.productBizId(), position);
            return position;
        }

        @Override
        public Optional<Position> findByDimension(String portfolioBizId, String productBizId, String positionSide) {
            return Optional.ofNullable(positions.get(productBizId))
                .filter(position -> position.deleted() == 0);
        }

        @Override
        public List<Position> findByPortfolioBizId(String portfolioBizId) {
            return positions.values().stream()
                .filter(position -> position.deleted() == 0 && position.quantity().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        }
    }

    /** 内存估值仓储。 */
    private static final class InMemoryValuationStore implements PortfolioValuationStore {
        private PortfolioValuation latest = PortfolioValuation.builder()
            .bizId("valuation-1")
            .portfolioBizId("portfolio-1")
            .valuationTime(NOW.minusDays(1))
            .baseCurrency("CNY")
            .totalAsset(new BigDecimal("20000"))
            .cashBalance(new BigDecimal("10000"))
            .positionValue(new BigDecimal("10000"))
            .totalCost(new BigDecimal("10000"))
            .unrealizedProfit(BigDecimal.ZERO)
            .realizedProfit(BigDecimal.ZERO)
            .totalReturnRate(BigDecimal.ZERO)
            .sourceCode("MOCK_BUY_FILLED")
            .createdAt(NOW.minusDays(1))
            .build();

        @Override
        public PortfolioValuation save(PortfolioValuation valuation) {
            latest = valuation;
            return valuation;
        }

        @Override
        public Optional<PortfolioValuation> findLatestByPortfolioBizId(String portfolioBizId) {
            return Optional.of(latest);
        }

        @Override
        public Optional<PortfolioValuation> findFirstByPortfolioBizId(String portfolioBizId) {
            return Optional.of(latest);
        }

        @Override
        public List<PortfolioValuation> findHistoryByPortfolioBizId(String portfolioBizId, int limit) {
            return List.of(latest);
        }
    }

    /** 内存订单仓储。 */
    private static final class InMemoryOrderStore implements MockOrderStore {
        private final Map<String, MockOrder> orders = new HashMap<>();
        private final Map<String, MockOrder> idempotencyOrders = new HashMap<>();

        @Override
        public MockOrder save(MockOrder order) {
            orders.put(order.bizId(), order);
            if (order.idempotencyKey() != null) {
                idempotencyOrders.put(order.userBizId() + ":" + order.idempotencyKey(), order);
            }
            return order;
        }

        @Override
        public Optional<MockOrder> findByUserAndIdempotencyKey(String userBizId, String idempotencyKey) {
            return Optional.ofNullable(idempotencyOrders.get(userBizId + ":" + idempotencyKey));
        }

        @Override
        public Optional<MockOrder> findByBizId(String orderBizId) {
            return Optional.ofNullable(orders.get(orderBizId));
        }
    }

    /** 内存订单事件仓储。 */
    private static final class InMemoryOrderEventStore implements OrderEventStore {
        private final List<OrderEvent> events = new ArrayList<>();

        @Override
        public OrderEvent save(OrderEvent event) {
            events.add(event);
            return event;
        }

        @Override
        public List<OrderEvent> findByOrderBizId(String orderBizId) {
            return events.stream()
                .filter(event -> orderBizId.equals(event.orderBizId()))
                .toList();
        }
    }

    /** 内存成交仓储。 */
    private static final class InMemoryExecutionStore implements TradeExecutionStore {
        private final Map<String, TradeExecution> executions = new HashMap<>();

        @Override
        public TradeExecution save(TradeExecution execution) {
            executions.put(execution.orderBizId(), execution);
            return execution;
        }

        @Override
        public Optional<TradeExecution> findFirstByOrderBizId(String orderBizId) {
            return Optional.ofNullable(executions.get(orderBizId));
        }
    }

    /** 固定产品仓储。 */
    private static final class InMemoryProductStore implements ProductStore {
        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public Optional<Product> findByBizId(String bizId) {
            return Optional.of(Product.builder()
                .bizId(bizId)
                .productNo("P-" + bizId)
                .productCode("CODE-" + bizId)
                .productName("产品")
                .productType(ProductType.FUND)
                .marketCode("CN")
                .currency("CNY")
                .tradeStatus(ProductTradeStatus.TRADABLE)
                .riskLevel(3)
                .minInvestAmount(BigDecimal.ZERO)
                .amountStep(BigDecimal.ZERO)
                .quantityStep(BigDecimal.ZERO)
                .feeRate(new BigDecimal("0.0001"))
                .createdAt(NOW)
                .updatedAt(NOW)
                .createdBy("user-1")
                .updatedBy("user-1")
                .deleted(0)
                .build());
        }

        @Override
        public boolean existsByMarketAndCode(String marketCode, String productCode) {
            return false;
        }

        @Override
        public Optional<Product> findByMarketAndCode(String marketCode, String productCode) {
            return Optional.empty();
        }

        @Override
        public PageResult<Product> search(ProductSearchCriteria criteria) {
            List<Product> items = List.of(findByBizId("product-1").orElseThrow());
            return PageResult.<Product>builder()
                .items(items)
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(1)
                .build();
        }
    }

    /** 固定产品画像仓储。 */
    private static final class FixedProfileStore implements ProductInvestmentProfileStore {
        @Override
        public ProductInvestmentProfile save(ProductInvestmentProfile profile) {
            return profile;
        }

        @Override
        public Optional<ProductInvestmentProfile> findByProductBizId(String productBizId) {
            return Optional.of(ProductInvestmentProfile.builder()
                .bizId("profile-" + productBizId)
                .productBizId(productBizId)
                .assetClass("FUND")
                .suitableRiskLevel(3)
                .mockTradable(true)
                .dataQualityScore(new BigDecimal("0.80"))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());
        }
    }

    /** 固定行情仓储。 */
    private static final class FixedQuoteStore implements MarketQuoteStore {
        @Override
        public MarketQuote savePoint(MarketQuote quote) {
            return quote;
        }

        @Override
        public Optional<MarketQuote> findLatest(String productBizId, String interval, String sourceCode) {
            return Optional.of(MarketQuote.builder()
                .bizId("quote-" + productBizId)
                .productBizId(productBizId)
                .sourceCode("TEST")
                .interval("1D")
                .quoteTime(NOW)
                .closePrice(new BigDecimal("1.250246913"))
                .status(QuoteStatus.VALID)
                .receivedAt(NOW)
                .createdAt(NOW)
                .build());
        }

        @Override
        public List<MarketQuote> findHistory(
            String productBizId,
            String interval,
            String sourceCode,
            LocalDateTime from,
            LocalDateTime to,
            int limit
        ) {
            return List.of();
        }

        @Override
        public List<ThemeProductPerformance> findPerformance(
            List<String> productCodes,
            LocalDateTime from,
            LocalDateTime to
        ) {
            return List.of();
        }
    }

    /** 空报告仓储。 */
    private static final class InMemoryAnalysisReportStore implements InvestmentAnalysisReportStore {
        private final Map<String, InvestmentAnalysisReport> reports = new HashMap<>();

        @Override
        public InvestmentAnalysisReport save(InvestmentAnalysisReport report) {
            reports.put(report.bizId(), report);
            return report;
        }

        @Override
        public Optional<InvestmentAnalysisReport> findByBizId(String bizId) {
            return Optional.ofNullable(reports.get(bizId));
        }

        @Override
        public PageResult<InvestmentAnalysisReport> search(
            com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria criteria
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PageResult<InvestmentAnalysisReport> latest(int size) {
            return PageResult.<InvestmentAnalysisReport>builder()
                .items(reports.values().stream().limit(size).toList())
                .total(reports.size())
                .page(1)
                .size(size)
                .totalPages(reports.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 空主题关系仓储。 */
    private static final class EmptyThemeRelationStore implements ProductThemeRelationStore {
        @Override
        public void replaceByProductBizId(String productBizId, List<ProductThemeRelation> relations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ProductThemeRelation> findByProductBizId(String productBizId) {
            return List.of();
        }

        @Override
        public List<ProductThemeRelation> findByRelation(String relationType, String relationCode) {
            return List.of();
        }
    }

    /** 递增业务 ID 生成器。 */
    private static final class IncrementalIdGenerator implements IdGenerator {
        private int sequence;

        @Override
        public String newBizId() {
            sequence++;
            return "00000000-0000-0000-0000-" + String.format("%012d", sequence);
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }
}
