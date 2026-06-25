package com.example.dzcom.application.service.portfolio;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.dzcom.application.assembler.portfolio.MockOrderExecutionViewAssembler;
import com.example.dzcom.application.assembler.portfolio.MockPortfolioViewAssembler;
import com.example.dzcom.application.command.portfolio.CancelMockOrderCommand;
import com.example.dzcom.application.command.portfolio.CreateMockPortfolioCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockBuyCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockRebalanceCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockSellCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockOrderView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioPerformanceView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.dto.portfolio.MockRebalanceExecutionView;
import com.example.dzcom.application.dto.portfolio.OrderEventView;
import com.example.dzcom.application.dto.portfolio.PortfolioValuationView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.risk.RiskAuditApplicationService;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.portfolio.MockOrder;
import com.example.dzcom.domain.model.portfolio.OrderEvent;
import com.example.dzcom.domain.model.portfolio.Portfolio;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.model.portfolio.Position;
import com.example.dzcom.domain.model.portfolio.TradeExecution;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductThemeRelation;
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
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.product.ProductThemeRelationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 模拟投资组合应用服务。 */
@Service
@RequiredArgsConstructor
public class MockPortfolioApplicationService {
    private static final String PORTFOLIO_TYPE_SIMULATION = "SIMULATION";
    private static final String DEFAULT_CURRENCY = "CNY";
    private static final String CHANNEL_SIMULATOR = "SIMULATOR";
    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "updatedAt", "portfolioNo", "portfolioName");

    private final PortfolioStore portfolios;
    private final PositionStore positions;
    private final PortfolioValuationStore valuations;
    private final MockOrderStore orders;
    private final OrderEventStore orderEvents;
    private final TradeExecutionStore executions;
    private final ProductStore products;
    private final ProductInvestmentProfileStore investmentProfiles;
    private final ProductThemeRelationStore productThemeRelations;
    private final InvestmentAnalysisReportStore analysisReports;
    private final MarketQuoteStore quotes;
    private final MockPortfolioViewAssembler assembler;
    private final MockOrderExecutionViewAssembler executionAssembler;
    private final CurrentOperatorProvider currentOperator;
    private final RiskAuditApplicationService riskAudits;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 创建当前用户的模拟组合，并写入初始现金估值快照。
     *
     * <p>当前阶段不生成订单和成交，只创建可被前端展示的模拟组合容器。后续投资方案
     * 通过 Mock 订单进入该组合时，会继续更新持仓和估值。</p>
     *
     * @param command 创建模拟组合命令
     * @return 创建后的模拟组合详情
     * @throws BusinessException 当名称、币种或初始资金不合法时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockPortfolioView create(CreateMockPortfolioCommand command) {
        CurrentOperator operator = currentOperator.required();
        return createForUser(command, operator.userBizId(), operator.userBizId());
    }

    /**
     * 为指定用户创建或获取自动闭环模拟组合。
     *
     * <p>该入口只供系统自动闭环任务使用，真实交易仍被禁止。它避免伪造 HTTP Session，
     * 同时复用同一套组合、估值和后续 Mock 交易门禁。</p>
     *
     * @param userBizId 组合归属用户业务标识
     * @param portfolioName 组合名称
     * @param initialCash 初始现金
     * @return 已存在或新创建的模拟组合
     * @author dz
     * @date 2026-06-25
     */
    @Transactional
    public MockPortfolioView ensureAutomationPortfolio(String userBizId, String portfolioName, BigDecimal initialCash) {
        String normalizedName = normalizeName(portfolioName);
        PageResult<Portfolio> page = portfolios.search(new PortfolioSearchCriteria(
            userBizId,
            PORTFOLIO_TYPE_SIMULATION,
            1,
            1,
            100,
            "createdAt",
            true
        ));
        Portfolio existed = page.items().stream()
            .filter(portfolio -> normalizedName.equals(portfolio.portfolioName()))
            .findFirst()
            .orElse(null);
        if (existed != null) {
            return assembler.assembleDetail(
                existed,
                valuations.findLatestByPortfolioBizId(existed.bizId()),
                positions.findByPortfolioBizId(existed.bizId())
            );
        }
        return createForUser(CreateMockPortfolioCommand.builder()
            .portfolioName(normalizedName)
            .baseCurrency(DEFAULT_CURRENCY)
            .initialCash(initialCash)
            .build(), userBizId, "AUTO_CLOSED_LOOP");
    }

    /** 为指定用户创建模拟组合。 */
    private MockPortfolioView createForUser(CreateMockPortfolioCommand command, String ownerUserBizId, String createdBy) {
        String portfolioName = normalizeName(command.portfolioName());
        String baseCurrency = normalizeCurrency(command.baseCurrency());
        BigDecimal initialCash = normalizeInitialCash(command.initialCash());
        LocalDateTime now = clock.now();
        Portfolio portfolio = Portfolio.builder()
            .bizId(ids.newBizId())
            .portfolioNo(newPortfolioNo())
            .ownerUserBizId(ownerUserBizId)
            .portfolioName(portfolioName)
            .portfolioType(PORTFOLIO_TYPE_SIMULATION)
            .baseCurrency(baseCurrency)
            .status(1)
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy)
            .updatedBy(createdBy)
            .deleted(0)
            .deletedAt(null)
            .build();
        Portfolio saved = portfolios.save(portfolio);
        PortfolioValuation valuation = valuations.save(initialValuation(saved, initialCash, now));
        return assembler.assembleDetail(saved, java.util.Optional.of(valuation), java.util.List.of());
    }

    /**
     * 分页查询当前用户的模拟组合。
     *
     * @param query 分页和排序参数
     * @return 模拟组合分页视图
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public PageResult<MockPortfolioView> listMine(PageQuery query) {
        CurrentOperator operator = currentOperator.required();
        PortfolioSearchCriteria criteria = new PortfolioSearchCriteria(
            operator.userBizId(),
            PORTFOLIO_TYPE_SIMULATION,
            1,
            query.page(),
            query.size(),
            query.safeSort(SORT_FIELDS, "createdAt"),
            "asc".equalsIgnoreCase(query.direction())
        );
        PageResult<Portfolio> page = portfolios.search(criteria);
        return PageResult.<MockPortfolioView>builder()
            .items(page.items().stream()
                .map(portfolio -> assembler.assembleSummary(
                    portfolio,
                    valuations.findLatestByPortfolioBizId(portfolio.bizId())
                ))
                .toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 查询当前用户的模拟组合详情。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 模拟组合详情视图
     * @throws BusinessException 当组合不存在或不属于当前用户时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public MockPortfolioView detail(String portfolioBizId) {
        CurrentOperator operator = currentOperator.required();
        Portfolio portfolio = requiredOwnedSimulationPortfolio(portfolioBizId, operator);
        return assembler.assembleDetail(
            portfolio,
            valuations.findLatestByPortfolioBizId(portfolio.bizId()),
            positions.findByPortfolioBizId(portfolio.bizId())
        );
    }

    /**
     * 执行模拟金额买入，并立即生成订单、成交、持仓和估值快照。
     *
     * <p>该用例是 Mock 交易闭环的第一条可验证链路。它只使用模拟渠道，
     * 不调用真实交易接口；产品必须允许 Mock 交易，组合必须属于当前用户，
     * 且必须存在最新 1D 行情作为成交价格。</p>
     *
     * @param command 模拟买入命令
     * @return 模拟买入执行结果
     * @throws BusinessException 当组合、产品、行情、现金或 Mock 门禁不满足要求时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockOrderExecutionView buy(ExecuteMockBuyCommand command) {
        CurrentOperator operator = currentOperator.required();
        Portfolio portfolio = requiredOwnedSimulationPortfolio(command.portfolioBizId(), operator);
        Product product = requiredTradableProduct(command.productBizId());
        requireMockTradable(product.getBizId(), operator.userBizId(), "ORDER", portfolio.bizId());
        BigDecimal amount = normalizeBuyAmount(command.amount());
        MarketQuote quote = quotes.findLatest(product.getBizId(), "1D", null)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "产品缺少最新行情，不能模拟成交"));
        BigDecimal price = quote.closePrice();
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            rejectWithAudit(operator.userBizId(), "ORDER", portfolio.bizId(), "MOCK_QUOTE_PRICE",
                "HIGH", "INVALID_QUOTE_PRICE", "产品最新行情价格无效，不能模拟成交",
                Map.of("productBizId", product.getBizId(), "price", price));
        }
        BigDecimal quantity = amount.divide(price, 8, RoundingMode.DOWN);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            rejectWithAudit(operator.userBizId(), "ORDER", portfolio.bizId(), "MOCK_ORDER_AMOUNT",
                "MEDIUM", "INVALID_TRADE_QUANTITY", "买入金额不足以形成有效成交数量",
                Map.of("productBizId", product.getBizId(), "amount", amount, "price", price));
        }
        BigDecimal feeAmount = amount.multiply(defaultZero(product.getFeeRate()))
            .setScale(8, RoundingMode.HALF_UP);
        PortfolioValuation latest = valuations.findLatestByPortfolioBizId(portfolio.bizId())
            .orElseGet(() -> initialValuation(portfolio, BigDecimal.ZERO, clock.now()));
        BigDecimal requiredCash = amount.add(feeAmount);
        if (latest.cashBalance().compareTo(requiredCash) < 0) {
            rejectWithAudit(operator.userBizId(), "ORDER", portfolio.bizId(), "MOCK_CASH_BALANCE",
                "HIGH", "INSUFFICIENT_CASH", "模拟组合现金不足",
                Map.of("portfolioBizId", portfolio.bizId(), "cashBalance", latest.cashBalance(),
                    "requiredCash", requiredCash));
        }
        MockOrder existingOrder = orders
            .findByUserAndIdempotencyKey(operator.userBizId(), command.idempotencyKey())
            .orElse(null);
        if (existingOrder != null) {
            TradeExecution existingExecution = executions.findFirstByOrderBizId(existingOrder.bizId())
                .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "幂等订单缺少成交记录"));
            MockPortfolioView portfolioView = detail(existingOrder.portfolioBizId());
            return executionAssembler.assemble(existingOrder, existingExecution, portfolioView);
        }
        LocalDateTime now = clock.now();
        MockOrder order = orders.save(filledBuyOrder(
            operator,
            portfolio,
            product,
            command.idempotencyKey(),
            price,
            quantity,
            amount,
            feeAmount,
            now
        ));
        TradeExecution execution = executions.save(execution(order, price, quantity, amount, feeAmount, now));
        appendOrderEvent(order, null, "FILLED", "FILLED", "INTERNAL", filledEventPayload(order, execution));
        Position position = positions.save(updatedLongPosition(portfolio, product, quantity, amount, now));
        PortfolioValuation valuation = valuations.save(nextValuation(portfolio, latest, amount, feeAmount, now));
        MockPortfolioView portfolioView = assembler.assembleDetail(
            portfolio,
            java.util.Optional.of(valuation),
            positions.findByPortfolioBizId(position.portfolioBizId())
        );
        return executionAssembler.assemble(order, execution, portfolioView);
    }

    /**
     * 执行模拟数量卖出，并立即生成订单、成交、持仓和估值快照。
     *
     * <p>卖出只影响模拟组合，不触发真实交易。当前实现按产品最新 1D 收盘价即时成交，
     * 并要求持仓可用数量充足；成交后现金增加、持仓数量和成本按平均成本法扣减。</p>
     *
     * @param command 模拟卖出命令
     * @return 模拟卖出执行结果
     * @throws BusinessException 当组合、产品、行情、持仓或 Mock 门禁不满足要求时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockOrderExecutionView sell(ExecuteMockSellCommand command) {
        CurrentOperator operator = currentOperator.required();
        Portfolio portfolio = requiredOwnedSimulationPortfolio(command.portfolioBizId(), operator);
        Product product = requiredTradableProduct(command.productBizId());
        requireMockTradable(product.getBizId(), operator.userBizId(), "ORDER", portfolio.bizId());
        BigDecimal quantity = normalizeSellQuantity(command.quantity());
        MarketQuote quote = latestValidQuote(product.getBizId(), "产品缺少最新行情，不能模拟卖出");
        BigDecimal price = quote.closePrice();
        BigDecimal amount = quantity.multiply(price).setScale(8, RoundingMode.HALF_UP);
        BigDecimal feeAmount = amount.multiply(defaultZero(product.getFeeRate()))
            .setScale(8, RoundingMode.HALF_UP);
        Position existingPosition = positions.findByDimension(portfolio.bizId(), product.getBizId(), "LONG")
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模拟组合没有该产品持仓"));
        if (existingPosition.availableQuantity().compareTo(quantity) < 0) {
            rejectWithAudit(operator.userBizId(), "ORDER", portfolio.bizId(), "MOCK_POSITION_AVAILABLE",
                "HIGH", "INSUFFICIENT_POSITION", "模拟组合可卖出持仓不足",
                Map.of("portfolioBizId", portfolio.bizId(), "productBizId", product.getBizId(),
                    "availableQuantity", existingPosition.availableQuantity(), "sellQuantity", quantity));
        }
        MockOrder existingOrder = orders
            .findByUserAndIdempotencyKey(operator.userBizId(), command.idempotencyKey())
            .orElse(null);
        if (existingOrder != null) {
            TradeExecution existingExecution = executions.findFirstByOrderBizId(existingOrder.bizId())
                .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "幂等订单缺少成交记录"));
            MockPortfolioView portfolioView = detail(existingOrder.portfolioBizId());
            return executionAssembler.assemble(existingOrder, existingExecution, portfolioView);
        }
        PortfolioValuation latest = valuations.findLatestByPortfolioBizId(portfolio.bizId())
            .orElseGet(() -> initialValuation(portfolio, BigDecimal.ZERO, clock.now()));
        LocalDateTime now = clock.now();
        MockOrder order = orders.save(filledSellOrder(
            operator,
            portfolio,
            product,
            command.idempotencyKey(),
            price,
            quantity,
            amount,
            feeAmount,
            now
        ));
        TradeExecution execution = executions.save(execution(order, price, quantity, amount, feeAmount, now));
        appendOrderEvent(order, null, "FILLED", "FILLED", "INTERNAL", filledEventPayload(order, execution));
        Position position = positions.save(reducedLongPosition(existingPosition, quantity, amount, feeAmount, now));
        BigDecimal realizedProfitIncrement = sellRealizedProfitIncrement(existingPosition, quantity, amount, feeAmount);
        PortfolioValuation valuation = valuations.save(nextSellValuation(
            portfolio,
            latest,
            position,
            amount,
            feeAmount,
            realizedProfitIncrement,
            now
        ));
        MockPortfolioView portfolioView = assembler.assembleDetail(
            portfolio,
            java.util.Optional.of(valuation),
            positions.findByPortfolioBizId(position.portfolioBizId())
        );
        return executionAssembler.assemble(order, execution, portfolioView);
    }

    /**
     * 撤销当前用户拥有的非终态模拟订单。
     *
     * <p>当前 Mock 买入和卖出均为即时成交，因此大多数订单已经处于 {@code FILLED}
     * 终态，撤单会被明确拒绝。该接口先把撤单边界暴露给前端，便于后续接入
     * 部分成交、排队成交或人工复核订单时复用。</p>
     *
     * @param command 撤销模拟订单命令
     * @return 撤销后的模拟订单视图
     * @throws BusinessException 当订单不存在、越权或已处于终态时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockOrderView cancelOrder(CancelMockOrderCommand command) {
        CurrentOperator operator = currentOperator.required();
        MockOrder order = orders.findByBizId(command.orderBizId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模拟订单不存在"));
        if (!operator.userBizId().equals(order.userBizId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权撤销该模拟订单");
        }
        if (isTerminalOrderStatus(order.status())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "订单已处于终态，不能撤销");
        }
        LocalDateTime now = clock.now();
        MockOrder cancelled = orders.save(order.toBuilder()
            .status("CANCELLED")
            .rejectCode("USER_CANCELLED")
            .rejectMessage(normalizeCancelReason(command.cancelReason()))
            .completedAt(now)
            .updatedAt(now)
            .version(order.version() + 1)
            .build());
        appendOrderEvent(cancelled, order.status(), "CANCELLED", "CANCELLED", "OPERATOR", JSON.toJSONString(Map.of(
            "reason", cancelled.rejectMessage(),
            "rejectCode", cancelled.rejectCode()
        )));
        return executionAssembler.assembleOrder(cancelled);
    }

    /**
     * 查询当前用户可见的模拟订单事件。
     *
     * <p>订单事件是交易链路的前端可查审计出口。查询前会校验订单归属当前用户，
     * 避免通过事件接口推断他人的组合、产品或成交信息。</p>
     *
     * @param orderBizId 订单业务唯一标识
     * @return 订单生命周期事件集合
     * @throws BusinessException 当订单不存在或不属于当前用户时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public List<OrderEventView> orderEvents(String orderBizId) {
        CurrentOperator operator = currentOperator.required();
        MockOrder order = orders.findByBizId(orderBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模拟订单不存在"));
        if (!operator.userBizId().equals(order.userBizId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权查看该模拟订单事件");
        }
        return orderEvents.findByOrderBizId(orderBizId).stream()
            .map(this::toOrderEventView)
            .toList();
    }

    /**
     * 从投资分析报告中的参考配置金额生成模拟买入。
     *
     * <p>该用例把“分析报告”推进到“可验证 Mock 交易”。报告必须通过数据质量门禁，
     * 投资方案不能是数据缺口报告，且必须能解析出大于 0 的参考配置金额。产品可以由
     * 前端指定；未指定时按报告主题反查产品主题关系并选择权重最高的产品。</p>
     *
     * @param command 从报告执行模拟买入命令
     * @return 模拟订单、成交和成交后的组合详情
     * @throws BusinessException 当报告、产品、金额或数据质量不满足要求时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockOrderExecutionView buyFromReport(ExecuteMockPlanFromReportCommand command) {
        InvestmentAnalysisReport report = analysisReports.findByBizId(command.reportBizId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "投资分析报告不存在"));
        CurrentOperator operator = currentOperator.required();
        ensureReportExecutable(report, operator.userBizId());
        JSONObject plan = JSON.parseObject(report.investmentPlan());
        BigDecimal amount = plan.getBigDecimal("referenceAllocationAmount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            rejectWithAudit(operator.userBizId(), "REPORT", report.bizId(), "REPORT_EXECUTABLE_AMOUNT",
                "HIGH", "NO_EXECUTABLE_AMOUNT", "报告未给出可执行的参考配置金额",
                Map.of("reportBizId", report.bizId()));
        }
        String productBizId = resolveReportProductBizId(report, command.productBizId());
        String idempotencyKey = command.idempotencyKey() == null || command.idempotencyKey().isBlank()
            ? "REPORT-" + report.bizId() + "-" + productBizId
            : command.idempotencyKey();
        return buy(ExecuteMockBuyCommand.builder()
            .portfolioBizId(command.portfolioBizId())
            .productBizId(productBizId)
            .amount(amount)
            .idempotencyKey(idempotencyKey)
            .build());
    }

    /**
     * 按目标权重执行模拟组合再平衡。
     *
     * <p>再平衡以最新估值和最新行情计算当前资产占比。目标权重总和允许小于 1，
     * 剩余部分保留现金；目标集合未包含的现有持仓视为目标权重 0，会先卖出释放现金，
     * 然后再买入低配产品。</p>
     *
     * @param command 模拟再平衡命令
     * @return 本次调仓订单集合和最终组合详情
     * @throws BusinessException 当目标权重、行情、现金或持仓不满足要求时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockRebalanceExecutionView rebalance(ExecuteMockRebalanceCommand command) {
        CurrentOperator operator = currentOperator.required();
        Portfolio portfolio = requiredOwnedSimulationPortfolio(command.portfolioBizId(), operator);
        Map<String, BigDecimal> targetWeights = normalizeTargetWeights(command.targets(), operator.userBizId(), command.portfolioBizId());
        BigDecimal minTradeAmount = normalizeMinTradeAmount(command.minTradeAmount());
        PortfolioValuation latest = valuations.findLatestByPortfolioBizId(portfolio.bizId())
            .orElseGet(() -> initialValuation(portfolio, BigDecimal.ZERO, clock.now()));
        List<Position> currentPositions = positions.findByPortfolioBizId(portfolio.bizId());
        Map<String, Position> positionByProduct = currentPositions.stream()
            .collect(Collectors.toMap(Position::productBizId, Function.identity()));
        Map<String, BigDecimal> priceByProduct = currentPrices(targetWeights, currentPositions);
        BigDecimal positionValue = currentPositions.stream()
            .map(position -> position.quantity().multiply(priceByProduct.get(position.productBizId()))
                .setScale(8, RoundingMode.HALF_UP))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAsset = latest.cashBalance().add(positionValue);
        if (totalAsset.compareTo(BigDecimal.ZERO) <= 0) {
            rejectWithAudit(operator.userBizId(), "PORTFOLIO", portfolio.bizId(), "MOCK_REBALANCE_ASSET",
                "HIGH", "INVALID_TOTAL_ASSET", "模拟组合总资产必须大于0才能再平衡",
                Map.of("portfolioBizId", portfolio.bizId(), "totalAsset", totalAsset));
        }
        List<MockOrderExecutionView> executionViews = new ArrayList<>();
        Map<String, BigDecimal> allProductWeights = new LinkedHashMap<>();
        positionByProduct.keySet().forEach(productBizId -> allProductWeights.put(productBizId, BigDecimal.ZERO));
        targetWeights.forEach(allProductWeights::put);
        int sequence = 1;
        for (Map.Entry<String, BigDecimal> entry : allProductWeights.entrySet()) {
            String productBizId = entry.getKey();
            BigDecimal currentAmount = positionByProduct.containsKey(productBizId)
                ? positionByProduct.get(productBizId).quantity().multiply(priceByProduct.get(productBizId))
                    .setScale(8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            BigDecimal targetAmount = totalAsset.multiply(entry.getValue()).setScale(8, RoundingMode.HALF_UP);
            BigDecimal diff = targetAmount.subtract(currentAmount);
            if (diff.compareTo(minTradeAmount.negate()) <= 0) {
                BigDecimal sellQuantity = diff.abs().divide(priceByProduct.get(productBizId), 8, RoundingMode.DOWN);
                if (sellQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    executionViews.add(sell(ExecuteMockSellCommand.builder()
                        .portfolioBizId(portfolio.bizId())
                        .productBizId(productBizId)
                        .quantity(sellQuantity)
                        .idempotencyKey(rebalanceLegIdempotency(command.idempotencyKey(), sequence, "SELL", productBizId))
                        .build()));
                    sequence++;
                }
            }
        }
        for (Map.Entry<String, BigDecimal> entry : targetWeights.entrySet()) {
            String productBizId = entry.getKey();
            BigDecimal currentAmount = positionByProduct.containsKey(productBizId)
                ? positionByProduct.get(productBizId).quantity().multiply(priceByProduct.get(productBizId))
                    .setScale(8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            BigDecimal targetAmount = totalAsset.multiply(entry.getValue()).setScale(8, RoundingMode.HALF_UP);
            BigDecimal diff = targetAmount.subtract(currentAmount);
            if (diff.compareTo(minTradeAmount) >= 0) {
                executionViews.add(buy(ExecuteMockBuyCommand.builder()
                    .portfolioBizId(portfolio.bizId())
                    .productBizId(productBizId)
                    .amount(diff)
                    .idempotencyKey(rebalanceLegIdempotency(command.idempotencyKey(), sequence, "BUY", productBizId))
                    .build()));
                sequence++;
            }
        }
        return MockRebalanceExecutionView.builder()
            .executions(executionViews)
            .portfolio(detail(portfolio.bizId()))
            .build();
    }

    /**
     * 按当前持仓和最新行情刷新模拟组合估值。
     *
     * <p>该用例让前端可以主动生成新的估值点。持仓市值按每个产品最新 1D 收盘价计算；
     * 如果某个持仓缺少行情，直接拒绝刷新，避免收益曲线混入虚假价格。</p>
     *
     * @param portfolioBizId 模拟组合业务唯一标识
     * @return 刷新后的模拟组合详情
     * @throws BusinessException 当组合不存在、越权或持仓行情缺失时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional
    public MockPortfolioView refreshValuation(String portfolioBizId) {
        CurrentOperator operator = currentOperator.required();
        Portfolio portfolio = requiredOwnedSimulationPortfolio(portfolioBizId, operator);
        PortfolioValuation latest = valuations.findLatestByPortfolioBizId(portfolio.bizId())
            .orElseGet(() -> initialValuation(portfolio, BigDecimal.ZERO, clock.now()));
        List<Position> currentPositions = positions.findByPortfolioBizId(portfolio.bizId());
        BigDecimal positionValue = currentPositions.stream()
            .map(this::currentPositionValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = currentPositions.stream()
            .map(Position::costAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unrealizedProfit = positionValue.subtract(totalCost);
        BigDecimal totalAsset = latest.cashBalance().add(positionValue);
        PortfolioValuation first = valuations.findFirstByPortfolioBizId(portfolio.bizId())
            .orElse(latest);
        BigDecimal totalReturnRate = returnRate(totalAsset, first.totalAsset());
        LocalDateTime now = clock.now();
        PortfolioValuation refreshed = valuations.save(PortfolioValuation.builder()
            .bizId(ids.newBizId())
            .portfolioBizId(portfolio.bizId())
            .valuationTime(now)
            .baseCurrency(portfolio.baseCurrency())
            .totalAsset(totalAsset)
            .cashBalance(latest.cashBalance())
            .positionValue(positionValue)
            .totalCost(totalCost)
            .unrealizedProfit(unrealizedProfit)
            .realizedProfit(latest.realizedProfit())
            .totalReturnRate(totalReturnRate)
            .sourceCode("MOCK_MARK_TO_MARKET")
            .createdAt(now)
            .build());
        return assembler.assembleDetail(portfolio, java.util.Optional.of(refreshed), currentPositions);
    }

    /**
     * 查询模拟组合收益曲线和最大回撤。
     *
     * @param portfolioBizId 模拟组合业务唯一标识
     * @param limit 曲线点数量上限
     * @return 模拟组合收益曲线
     * @throws BusinessException 当组合不存在或越权时抛出
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public MockPortfolioPerformanceView performance(String portfolioBizId, Integer limit) {
        CurrentOperator operator = currentOperator.required();
        Portfolio portfolio = requiredOwnedSimulationPortfolio(portfolioBizId, operator);
        int safeLimit = normalizeCurveLimit(limit);
        List<PortfolioValuation> history = valuations.findHistoryByPortfolioBizId(portfolio.bizId(), safeLimit);
        return MockPortfolioPerformanceView.builder()
            .portfolioBizId(portfolio.bizId())
            .latestReturnRate(history.isEmpty()
                ? BigDecimal.ZERO
                : history.get(history.size() - 1).totalReturnRate())
            .maxDrawdown(maxDrawdown(history))
            .pointCount(history.size())
            .valuations(history.stream().map(this::toValuationView).toList())
            .build();
    }

    /**
     * 构建初始估值快照。
     *
     * @param portfolio 模拟组合
     * @param initialCash 初始现金
     * @param now 当前北京时间
     * @return 初始估值快照
     * @author dz
     * @date 2026-06-23
     */
    private PortfolioValuation initialValuation(Portfolio portfolio, BigDecimal initialCash, LocalDateTime now) {
        return PortfolioValuation.builder()
            .bizId(ids.newBizId())
            .portfolioBizId(portfolio.bizId())
            .valuationTime(now)
            .baseCurrency(portfolio.baseCurrency())
            .totalAsset(initialCash)
            .cashBalance(initialCash)
            .positionValue(BigDecimal.ZERO)
            .totalCost(BigDecimal.ZERO)
            .unrealizedProfit(BigDecimal.ZERO)
            .realizedProfit(BigDecimal.ZERO)
            .totalReturnRate(BigDecimal.ZERO)
            .sourceCode("MOCK_INITIAL_CASH")
            .createdAt(now)
            .build();
    }

    /**
     * 追加订单状态事件。
     *
     * @param order 模拟订单
     * @param fromStatus 变更前状态
     * @param toStatus 变更后状态
     * @param eventType 事件类型
     * @param eventSource 事件来源
     * @param payload 脱敏事件上下文
     * @author dz
     * @date 2026-06-23
     */
    private void appendOrderEvent(
        MockOrder order,
        String fromStatus,
        String toStatus,
        String eventType,
        String eventSource,
        String payload
    ) {
        LocalDateTime occurredAt = order.completedAt() == null ? clock.now() : order.completedAt();
        orderEvents.save(OrderEvent.builder()
            .bizId(ids.newBizId())
            .orderBizId(order.bizId())
            .eventType(eventType)
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .eventSource(eventSource)
            .operatorBizId(order.userBizId())
            .eventPayload(payload)
            .occurredAt(occurredAt)
            .createdAt(clock.now())
            .build());
    }

    /**
     * 构建成交事件上下文。
     *
     * @param order 模拟订单
     * @param execution 模拟成交
     * @return 脱敏后的成交事件 JSON
     * @author dz
     * @date 2026-06-23
     */
    private String filledEventPayload(MockOrder order, TradeExecution execution) {
        return JSON.toJSONString(Map.of(
            "orderSide", order.orderSide(),
            "orderType", order.orderType(),
            "productBizId", order.productBizId(),
            "portfolioBizId", order.portfolioBizId(),
            "executionBizId", execution.bizId(),
            "executionPrice", execution.executionPrice(),
            "executionQuantity", execution.executionQuantity(),
            "executionAmount", execution.executionAmount(),
            "feeAmount", execution.feeAmount()
        ));
    }

    /**
     * 转换订单事件视图。
     *
     * @param event 订单事件
     * @return 订单事件应用层视图
     * @author dz
     * @date 2026-06-23
     */
    private OrderEventView toOrderEventView(OrderEvent event) {
        return OrderEventView.builder()
            .bizId(event.bizId())
            .orderBizId(event.orderBizId())
            .eventType(event.eventType())
            .fromStatus(event.fromStatus())
            .toStatus(event.toStatus())
            .eventSource(event.eventSource())
            .operatorBizId(event.operatorBizId())
            .eventPayload(event.eventPayload())
            .occurredAt(event.occurredAt())
            .build();
    }

    /**
     * 计算单个持仓当前市值。
     *
     * @param position 当前持仓
     * @return 按最新行情计算的持仓市值
     * @throws BusinessException 当产品行情缺失或价格无效时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal currentPositionValue(Position position) {
        MarketQuote quote = latestValidQuote(position.productBizId(), "持仓产品缺少最新行情，不能刷新估值");
        return position.quantity().multiply(quote.closePrice()).setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 查询产品最新有效行情。
     *
     * @param productBizId 产品业务唯一标识
     * @param missingMessage 行情缺失时的业务提示
     * @return 最新有效行情
     * @throws BusinessException 当行情缺失或价格无效时抛出
     * @author dz
     * @date 2026-06-23
     */
    private MarketQuote latestValidQuote(String productBizId, String missingMessage) {
        MarketQuote quote = quotes.findLatest(productBizId, "1D", null)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, missingMessage));
        if (quote.closePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品最新行情价格无效，不能模拟成交");
        }
        return quote;
    }

    /**
     * 计算收益率。
     *
     * @param currentAsset 当前资产
     * @param baseAsset 基准资产
     * @return 收益率
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal returnRate(BigDecimal currentAsset, BigDecimal baseAsset) {
        if (baseAsset == null || baseAsset.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentAsset.subtract(baseAsset).divide(baseAsset, 10, RoundingMode.HALF_UP);
    }

    /**
     * 计算历史估值最大回撤。
     *
     * @param history 按估值时间升序排列的估值快照
     * @return 最大回撤，小数形式
     * @author dz
     * @date 2026-06-23
     */
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

    /**
     * 转换估值快照视图。
     *
     * @param valuation 估值快照领域对象
     * @return 估值快照视图
     * @author dz
     * @date 2026-06-23
     */
    private PortfolioValuationView toValuationView(PortfolioValuation valuation) {
        return PortfolioValuationView.builder()
            .valuationTime(valuation.valuationTime())
            .baseCurrency(valuation.baseCurrency())
            .totalAsset(valuation.totalAsset())
            .cashBalance(valuation.cashBalance())
            .positionValue(valuation.positionValue())
            .totalCost(valuation.totalCost())
            .unrealizedProfit(valuation.unrealizedProfit())
            .realizedProfit(valuation.realizedProfit())
            .totalReturnRate(valuation.totalReturnRate())
            .sourceCode(valuation.sourceCode())
            .build();
    }

    /**
     * 规范化收益曲线点数量。
     *
     * @param limit 请求数量
     * @return 安全数量
     * @author dz
     * @date 2026-06-23
     */
    private int normalizeCurveLimit(Integer limit) {
        if (limit == null) {
            return 120;
        }
        if (limit < 1 || limit > 500) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "收益曲线点数量必须在1到500之间");
        }
        return limit;
    }

    /**
     * 获取当前用户拥有的模拟组合。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param operator 当前操作者
     * @return 模拟组合
     * @throws BusinessException 当组合不存在、越权或类型不支持时抛出
     * @author dz
     * @date 2026-06-23
     */
    private Portfolio requiredOwnedSimulationPortfolio(String portfolioBizId, CurrentOperator operator) {
        Portfolio portfolio = portfolios.findByBizId(portfolioBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "模拟组合不存在"));
        if (!operator.userBizId().equals(portfolio.ownerUserBizId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权查看该模拟组合");
        }
        if (!PORTFOLIO_TYPE_SIMULATION.equals(portfolio.portfolioType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前接口只支持模拟组合");
        }
        return portfolio;
    }

    /**
     * 获取可交易产品。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品领域对象
     * @throws BusinessException 当产品不存在或不可交易时抛出
     * @author dz
     * @date 2026-06-23
     */
    private Product requiredTradableProduct(String productBizId) {
        Product product = products.findByBizId(productBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "产品不存在"));
        if (product.getTradeStatus() != ProductTradeStatus.TRADABLE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品当前不可交易");
        }
        return product;
    }

    /**
     * 校验产品是否允许进入 Mock 交易。
     *
     * @param productBizId 产品业务唯一标识
     * @throws BusinessException 当产品画像缺失或未开启 Mock 交易时抛出
     * @author dz
     * @date 2026-06-23
     */
    private void requireMockTradable(String productBizId, String userBizId, String businessType, String businessBizId) {
        boolean tradable = investmentProfiles.findByProductBizId(productBizId)
            .map(profile -> profile.mockTradable() && profile.dataQualityScore().compareTo(new BigDecimal("0.45")) >= 0)
            .orElse(false);
        if (!tradable) {
            rejectWithAudit(userBizId, businessType, businessBizId, "MOCK_PRODUCT_PROFILE",
                "HIGH", "PRODUCT_NOT_MOCK_TRADABLE", "产品投资画像不足或未开启Mock交易",
                Map.of("productBizId", productBizId));
        }
    }

    /**
     * 校验投资分析报告是否允许转为 Mock 交易。
     *
     * @param report 投资分析报告
     * @throws BusinessException 当报告质量不足或方案不可执行时抛出
     * @author dz
     * @date 2026-06-23
     */
    private void ensureReportExecutable(InvestmentAnalysisReport report, String userBizId) {
        if (!isReportSucceeded(report.status())) {
            rejectWithAudit(userBizId, "REPORT", report.bizId(), "REPORT_STATUS",
                "HIGH", "REPORT_NOT_SUCCESS", "报告未成功生成，不能执行Mock交易",
                auditDetail("reportBizId", report.bizId(), "status", report.status()));
        }
        if ("UNUSABLE".equals(report.confidenceLevel())) {
            rejectWithAudit(userBizId, "REPORT", report.bizId(), "REPORT_CONFIDENCE",
                "HIGH", "REPORT_UNUSABLE", "报告可信等级不可用，不能执行Mock交易",
                auditDetail("reportBizId", report.bizId(), "confidenceLevel", report.confidenceLevel()));
        }
        if (report.dataQualityScore() == null
            || report.dataQualityScore().compareTo(new BigDecimal("0.45")) < 0) {
            rejectWithAudit(userBizId, "REPORT", report.bizId(), "REPORT_DATA_QUALITY",
                "HIGH", "LOW_DATA_QUALITY", "报告数据质量不足，不能执行Mock交易",
                auditDetail("reportBizId", report.bizId(), "dataQualityScore", report.dataQualityScore()));
        }
        JSONObject gate = JSON.parseObject(report.dataQualityGate());
        if (!gate.getBooleanValue("passed")) {
            rejectWithAudit(userBizId, "REPORT", report.bizId(), "REPORT_DATA_GATE",
                "HIGH", "DATA_GATE_BLOCKED", "报告数据质量门禁未通过，不能执行Mock交易",
                Map.of("reportBizId", report.bizId(), "dataQualityGate", report.dataQualityGate()));
        }
        JSONObject plan = JSON.parseObject(report.investmentPlan());
        if ("DATA_GAP_REPORT".equals(plan.getString("planType"))) {
            rejectWithAudit(userBizId, "REPORT", report.bizId(), "REPORT_PLAN_TYPE",
                "HIGH", "DATA_GAP_REPORT", "数据缺口报告不能执行Mock交易",
                Map.of("reportBizId", report.bizId(), "planType", plan.getString("planType")));
        }
    }

    /** 兼容历史报告状态 SUCCESS 和当前自动报告状态 SUCCEEDED。 */
    private boolean isReportSucceeded(String status) {
        return "SUCCESS".equals(status) || "SUCCEEDED".equals(status);
    }

    /**
     * 根据报告主题选择待买入产品。
     *
     * @param report 投资分析报告
     * @param requestedProductBizId 前端指定的产品业务标识
     * @return 产品业务标识
     * @throws BusinessException 当无法根据主题找到产品时抛出
     * @author dz
     * @date 2026-06-23
     */
    private String resolveReportProductBizId(InvestmentAnalysisReport report, String requestedProductBizId) {
        if (requestedProductBizId != null && !requestedProductBizId.isBlank()) {
            return requestedProductBizId;
        }
        return productThemeRelations.findByRelation("THEME", report.themeCode()).stream()
            .findFirst()
            .map(ProductThemeRelation::productBizId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.BAD_REQUEST,
                "报告主题未关联可用于Mock交易的产品"
            ));
    }

    /**
     * 构建已成交的模拟买入订单。
     */
    private MockOrder filledBuyOrder(
        CurrentOperator operator,
        Portfolio portfolio,
        Product product,
        String idempotencyKey,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal amount,
        BigDecimal feeAmount,
        LocalDateTime now
    ) {
        return MockOrder.builder()
            .bizId(ids.newBizId())
            .orderNo(newOrderNo())
            .idempotencyKey(idempotencyKey)
            .userBizId(operator.userBizId())
            .portfolioBizId(portfolio.bizId())
            .productBizId(product.getBizId())
            .channelCode(CHANNEL_SIMULATOR)
            .orderSide("BUY")
            .orderType("AMOUNT")
            .currency(product.getCurrency())
            .requestedPrice(price)
            .requestedQuantity(quantity)
            .requestedAmount(amount)
            .executedQuantity(quantity)
            .executedAmount(amount)
            .feeAmount(feeAmount)
            .status("FILLED")
            .externalOrderId("SIM-" + ids.newBizId())
            .submittedAt(now)
            .completedAt(now)
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(operator.userBizId())
            .deleted(0)
            .build();
    }

    /**
     * 构建已成交的模拟卖出订单。
     *
     * @param operator 当前操作者
     * @param portfolio 模拟组合
     * @param product 产品
     * @param idempotencyKey 客户端幂等键
     * @param price 成交价格
     * @param quantity 成交数量
     * @param amount 成交金额
     * @param feeAmount 交易费用
     * @param now 当前业务时间
     * @return 已成交模拟卖出订单
     * @author dz
     * @date 2026-06-23
     */
    private MockOrder filledSellOrder(
        CurrentOperator operator,
        Portfolio portfolio,
        Product product,
        String idempotencyKey,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal amount,
        BigDecimal feeAmount,
        LocalDateTime now
    ) {
        return MockOrder.builder()
            .bizId(ids.newBizId())
            .orderNo(newOrderNo())
            .idempotencyKey(idempotencyKey)
            .userBizId(operator.userBizId())
            .portfolioBizId(portfolio.bizId())
            .productBizId(product.getBizId())
            .channelCode(CHANNEL_SIMULATOR)
            .orderSide("SELL")
            .orderType("MARKET")
            .currency(product.getCurrency())
            .requestedPrice(price)
            .requestedQuantity(quantity)
            .requestedAmount(amount)
            .executedQuantity(quantity)
            .executedAmount(amount)
            .feeAmount(feeAmount)
            .status("FILLED")
            .externalOrderId("SIM-" + ids.newBizId())
            .submittedAt(now)
            .completedAt(now)
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(operator.userBizId())
            .deleted(0)
            .build();
    }

    /** 构建模拟成交记录。 */
    private TradeExecution execution(
        MockOrder order,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal amount,
        BigDecimal feeAmount,
        LocalDateTime now
    ) {
        return TradeExecution.builder()
            .bizId(ids.newBizId())
            .executionNo(newExecutionNo())
            .orderBizId(order.bizId())
            .userBizId(order.userBizId())
            .portfolioBizId(order.portfolioBizId())
            .productBizId(order.productBizId())
            .channelCode(CHANNEL_SIMULATOR)
            .externalExecutionId("SIM-EXE-" + ids.newBizId())
            .executionPrice(price)
            .executionQuantity(quantity)
            .executionAmount(amount)
            .feeAmount(feeAmount)
            .executedAt(now)
            .createdAt(now)
            .build();
    }

    /** 更新或创建多头持仓。 */
    private Position updatedLongPosition(
        Portfolio portfolio,
        Product product,
        BigDecimal quantity,
        BigDecimal amount,
        LocalDateTime now
    ) {
        Position existing = positions.findByDimension(portfolio.bizId(), product.getBizId(), "LONG")
            .orElse(null);
        BigDecimal oldQuantity = existing == null ? BigDecimal.ZERO : existing.quantity();
        BigDecimal oldCostAmount = existing == null ? BigDecimal.ZERO : existing.costAmount();
        BigDecimal newQuantity = oldQuantity.add(quantity);
        BigDecimal newCostAmount = oldCostAmount.add(amount);
        BigDecimal averageCost = newCostAmount.divide(newQuantity, 8, RoundingMode.HALF_UP);
        return Position.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .portfolioBizId(portfolio.bizId())
            .productBizId(product.getBizId())
            .positionSide("LONG")
            .quantity(newQuantity)
            .availableQuantity((existing == null ? BigDecimal.ZERO : existing.availableQuantity()).add(quantity))
            .averageCost(averageCost)
            .costAmount(newCostAmount)
            .realizedProfit(existing == null ? BigDecimal.ZERO : existing.realizedProfit())
            .lastTradeAt(now)
            .version(existing == null ? 0 : existing.version() + 1)
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .deleted(0)
            .build();
    }

    /**
     * 按平均成本法扣减多头持仓。
     *
     * @param existing 当前持仓
     * @param sellQuantity 卖出数量
     * @param sellAmount 卖出成交金额
     * @param feeAmount 卖出费用
     * @param now 当前业务时间
     * @return 扣减后的当前持仓
     * @author dz
     * @date 2026-06-23
     */
    private Position reducedLongPosition(
        Position existing,
        BigDecimal sellQuantity,
        BigDecimal sellAmount,
        BigDecimal feeAmount,
        LocalDateTime now
    ) {
        BigDecimal remainingQuantity = existing.quantity().subtract(sellQuantity).max(BigDecimal.ZERO);
        BigDecimal reducedCost = existing.averageCost().multiply(sellQuantity).setScale(8, RoundingMode.HALF_UP);
        BigDecimal remainingCost = existing.costAmount().subtract(reducedCost).max(BigDecimal.ZERO);
        BigDecimal realizedProfit = existing.realizedProfit()
            .add(sellAmount.subtract(feeAmount).subtract(reducedCost))
            .setScale(8, RoundingMode.HALF_UP);
        BigDecimal averageCost = remainingQuantity.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : remainingCost.divide(remainingQuantity, 8, RoundingMode.HALF_UP);
        return Position.builder()
            .bizId(existing.bizId())
            .portfolioBizId(existing.portfolioBizId())
            .productBizId(existing.productBizId())
            .positionSide(existing.positionSide())
            .quantity(remainingQuantity)
            .availableQuantity(existing.availableQuantity().subtract(sellQuantity).max(BigDecimal.ZERO))
            .averageCost(averageCost)
            .costAmount(remainingCost)
            .realizedProfit(realizedProfit)
            .lastTradeAt(now)
            .version(existing.version() + 1)
            .createdAt(existing.createdAt())
            .updatedAt(now)
            .deleted(remainingQuantity.compareTo(BigDecimal.ZERO) == 0 ? 1 : 0)
            .build();
    }

    /**
     * 计算本次卖出产生的已实现盈亏增量。
     *
     * @param existing 卖出前持仓
     * @param sellQuantity 卖出数量
     * @param sellAmount 卖出成交金额
     * @param feeAmount 卖出费用
     * @return 本次卖出的已实现盈亏
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal sellRealizedProfitIncrement(
        Position existing,
        BigDecimal sellQuantity,
        BigDecimal sellAmount,
        BigDecimal feeAmount
    ) {
        BigDecimal reducedCost = existing.averageCost().multiply(sellQuantity).setScale(8, RoundingMode.HALF_UP);
        return sellAmount.subtract(feeAmount).subtract(reducedCost).setScale(8, RoundingMode.HALF_UP);
    }

    /** 构建成交后的估值快照。 */
    private PortfolioValuation nextValuation(
        Portfolio portfolio,
        PortfolioValuation latest,
        BigDecimal amount,
        BigDecimal feeAmount,
        LocalDateTime now
    ) {
        BigDecimal cashBalance = latest.cashBalance().subtract(amount).subtract(feeAmount);
        BigDecimal positionValue = latest.positionValue().add(amount);
        BigDecimal totalCost = latest.totalCost().add(amount);
        BigDecimal unrealizedProfit = positionValue.subtract(totalCost);
        BigDecimal totalAsset = cashBalance.add(positionValue);
        BigDecimal totalReturnRate = latest.totalAsset().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalAsset.subtract(latest.totalAsset()).divide(latest.totalAsset(), 10, RoundingMode.HALF_UP);
        return PortfolioValuation.builder()
            .bizId(ids.newBizId())
            .portfolioBizId(portfolio.bizId())
            .valuationTime(now)
            .baseCurrency(portfolio.baseCurrency())
            .totalAsset(totalAsset)
            .cashBalance(cashBalance)
            .positionValue(positionValue)
            .totalCost(totalCost)
            .unrealizedProfit(unrealizedProfit)
            .realizedProfit(latest.realizedProfit())
            .totalReturnRate(totalReturnRate)
            .sourceCode("MOCK_BUY_FILLED")
            .createdAt(now)
            .build();
    }

    /**
     * 构建卖出成交后的估值快照。
     *
     * @param portfolio 模拟组合
     * @param latest 最新估值快照
     * @param position 卖出后的持仓
     * @param amount 卖出成交金额
     * @param feeAmount 卖出费用
     * @param now 当前业务时间
     * @return 卖出后的估值快照
     * @author dz
     * @date 2026-06-23
     */
    private PortfolioValuation nextSellValuation(
        Portfolio portfolio,
        PortfolioValuation latest,
        Position position,
        BigDecimal amount,
        BigDecimal feeAmount,
        BigDecimal realizedProfitIncrement,
        LocalDateTime now
    ) {
        BigDecimal cashBalance = latest.cashBalance().add(amount).subtract(feeAmount);
        List<Position> currentPositions = positions.findByPortfolioBizId(portfolio.bizId()).stream()
            .filter(item -> !item.bizId().equals(position.bizId()))
            .collect(Collectors.toCollection(ArrayList::new));
        if (position.deleted() == 0 && position.quantity().compareTo(BigDecimal.ZERO) > 0) {
            currentPositions.add(position);
        }
        BigDecimal positionValue = currentPositions.stream()
            .map(this::currentPositionValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = currentPositions.stream()
            .map(Position::costAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realizedProfit = latest.realizedProfit().add(realizedProfitIncrement);
        BigDecimal unrealizedProfit = positionValue.subtract(totalCost);
        BigDecimal totalAsset = cashBalance.add(positionValue);
        BigDecimal totalReturnRate = latest.totalAsset().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalAsset.subtract(latest.totalAsset()).divide(latest.totalAsset(), 10, RoundingMode.HALF_UP);
        return PortfolioValuation.builder()
            .bizId(ids.newBizId())
            .portfolioBizId(portfolio.bizId())
            .valuationTime(now)
            .baseCurrency(portfolio.baseCurrency())
            .totalAsset(totalAsset)
            .cashBalance(cashBalance)
            .positionValue(positionValue)
            .totalCost(totalCost)
            .unrealizedProfit(unrealizedProfit)
            .realizedProfit(realizedProfit)
            .totalReturnRate(totalReturnRate)
            .sourceCode("MOCK_SELL_FILLED")
            .createdAt(now)
            .build();
    }

    /**
     * 校验并归一化再平衡目标权重。
     *
     * @param targets 目标权重集合
     * @return 产品到目标权重的有序映射
     * @throws BusinessException 当目标为空、重复、负数或权重总和超过1时抛出
     * @author dz
     * @date 2026-06-23
     */
    private Map<String, BigDecimal> normalizeTargetWeights(
        List<ExecuteMockRebalanceCommand.TargetWeight> targets,
        String userBizId,
        String portfolioBizId
    ) {
        if (targets == null || targets.isEmpty()) {
            rejectWithAudit(userBizId, "PORTFOLIO", portfolioBizId, "MOCK_REBALANCE_TARGET",
                "MEDIUM", "EMPTY_REBALANCE_TARGET", "再平衡目标权重不能为空", Map.of());
        }
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (ExecuteMockRebalanceCommand.TargetWeight target : targets) {
            if (target.productBizId() == null || target.productBizId().isBlank()) {
                rejectWithAudit(userBizId, "PORTFOLIO", portfolioBizId, "MOCK_REBALANCE_TARGET",
                    "MEDIUM", "EMPTY_TARGET_PRODUCT", "目标产品不能为空", Map.of());
            }
            if (result.containsKey(target.productBizId())) {
                rejectWithAudit(userBizId, "PORTFOLIO", portfolioBizId, "MOCK_REBALANCE_TARGET",
                    "MEDIUM", "DUPLICATE_TARGET_PRODUCT", "目标产品不能重复",
                    Map.of("productBizId", target.productBizId()));
            }
            BigDecimal weight = target.targetWeight();
            if (weight == null || weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(BigDecimal.ONE) > 0) {
                rejectWithAudit(userBizId, "PORTFOLIO", portfolioBizId, "MOCK_REBALANCE_TARGET",
                    "MEDIUM", "INVALID_TARGET_WEIGHT", "目标权重必须在0到1之间",
                    auditDetail("productBizId", target.productBizId(), "targetWeight", weight));
            }
            result.put(target.productBizId(), weight.setScale(10, RoundingMode.HALF_UP));
            sum = sum.add(weight);
        }
        if (sum.compareTo(BigDecimal.ONE) > 0) {
            rejectWithAudit(userBizId, "PORTFOLIO", portfolioBizId, "MOCK_REBALANCE_TARGET",
                "HIGH", "TARGET_WEIGHT_EXCEEDED", "目标权重总和不能超过1",
                Map.of("targetWeightSum", sum));
        }
        return result;
    }

    /**
     * 生成再平衡涉及产品的最新价格表。
     *
     * @param targetWeights 目标权重
     * @param currentPositions 当前持仓
     * @return 产品业务标识到最新价格的映射
     * @author dz
     * @date 2026-06-23
     */
    private Map<String, BigDecimal> currentPrices(Map<String, BigDecimal> targetWeights, List<Position> currentPositions) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        currentPositions.stream().map(Position::productBizId).forEach(productBizId ->
            result.put(productBizId, latestValidQuote(productBizId, "持仓产品缺少最新行情，不能再平衡").closePrice()));
        targetWeights.keySet().forEach(productBizId -> {
            Product product = requiredTradableProduct(productBizId);
            requireMockTradable(product.getBizId(), null, "PORTFOLIO", productBizId);
            result.put(productBizId, latestValidQuote(productBizId, "目标产品缺少最新行情，不能再平衡").closePrice());
        });
        return result;
    }

    /**
     * 记录风控拒绝并抛出业务异常。
     *
     * @param userBizId 用户业务标识
     * @param businessType 业务类型
     * @param businessBizId 业务对象
     * @param ruleCode 规则编码
     * @param riskLevel 风险等级
     * @param reasonCode 原因编码
     * @param message 前端错误提示
     * @param detail 脱敏检查详情
     * @author dz
     * @date 2026-06-23
     */
    private void rejectWithAudit(String userBizId, String businessType, String businessBizId,
                                 String ruleCode, String riskLevel, String reasonCode,
                                 String message, Map<String, Object> detail) {
        riskAudits.recordReject(userBizId, businessType, businessBizId, ruleCode, riskLevel, reasonCode, detail);
        throw new BusinessException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 构建可包含空值来源的审计详情，空值会被过滤避免 Map.of 空指针。
     *
     * @param values key/value 交替排列的审计字段
     * @return 脱敏审计详情
     * @author dz
     * @date 2026-06-23
     */
    private Map<String, Object> auditDetail(Object... values) {
        Map<String, Object> detail = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            if (values[i] != null && values[i + 1] != null) {
                detail.put(String.valueOf(values[i]), values[i + 1]);
            }
        }
        return detail;
    }

    /**
     * 生成再平衡单腿订单的幂等键。
     *
     * @param idempotencyKey 前端传入的幂等键前缀
     * @param sequence 调仓腿序号
     * @param side 订单方向
     * @param productBizId 产品业务唯一标识
     * @return 单腿幂等键
     * @author dz
     * @date 2026-06-23
     */
    private String rebalanceLegIdempotency(String idempotencyKey, int sequence, String side, String productBizId) {
        String prefix = idempotencyKey == null || idempotencyKey.isBlank()
            ? "REBALANCE-" + ids.newBizId()
            : idempotencyKey.trim();
        return prefix + "-" + sequence + "-" + side + "-" + productBizId;
    }

    /** 判断订单是否已进入不可撤销终态。 */
    private boolean isTerminalOrderStatus(String status) {
        return Set.of("FILLED", "CANCELLED", "REJECTED", "FAILED").contains(status);
    }

    /** 生成模拟订单编号。 */
    private String newOrderNo() {
        return "MO" + ids.newBizId().replace("-", "").substring(0, 18).toUpperCase();
    }

    /** 生成模拟成交编号。 */
    private String newExecutionNo() {
        return "ME" + ids.newBizId().replace("-", "").substring(0, 18).toUpperCase();
    }

    /**
     * 生成模拟组合展示编号。
     *
     * @return 模拟组合展示编号
     * @author dz
     * @date 2026-06-23
     */
    private String newPortfolioNo() {
        return "MP" + ids.newBizId().replace("-", "").substring(0, 18).toUpperCase();
    }

    /**
     * 规范化组合名称。
     *
     * @param value 原始组合名称
     * @return 去除首尾空白后的组合名称
     * @throws BusinessException 当名称为空时抛出
     * @author dz
     * @date 2026-06-23
     */
    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "模拟组合名称不能为空");
        }
        return value.trim();
    }

    /**
     * 规范化币种。
     *
     * @param value 原始币种
     * @return 大写币种
     * @author dz
     * @date 2026-06-23
     */
    private String normalizeCurrency(String value) {
        String currency = value == null || value.isBlank() ? DEFAULT_CURRENCY : value.trim();
        return currency.toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * 规范化初始模拟现金。
     *
     * @param value 原始初始现金
     * @return 初始模拟现金
     * @throws BusinessException 当初始现金为负数时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal normalizeInitialCash(BigDecimal value) {
        BigDecimal initialCash = value == null ? BigDecimal.ZERO : value;
        if (initialCash.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "初始模拟现金不能为负数");
        }
        return initialCash;
    }

    /**
     * 规范化买入金额。
     *
     * @param value 买入金额
     * @return 买入金额
     * @throws BusinessException 当金额为空或非正数时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal normalizeBuyAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "买入金额必须大于0");
        }
        return value.setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 规范化卖出数量。
     *
     * @param value 卖出数量
     * @return 卖出数量
     * @throws BusinessException 当数量为空或非正数时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal normalizeSellQuantity(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "卖出数量必须大于0");
        }
        return value.setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 规范化最小调仓金额。
     *
     * @param value 前端传入的最小调仓金额
     * @return 最小调仓金额
     * @throws BusinessException 当金额为负数时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal normalizeMinTradeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "最小调仓金额不能为负数");
        }
        return value.setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 规范化撤单原因。
     *
     * @param value 前端传入的撤单原因
     * @return 可展示的撤单原因
     * @author dz
     * @date 2026-06-23
     */
    private String normalizeCancelReason(String value) {
        if (value == null || value.isBlank()) {
            return "用户撤销模拟订单";
        }
        String trimmed = value.trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }

    /** 将空金额按 0 处理。 */
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
