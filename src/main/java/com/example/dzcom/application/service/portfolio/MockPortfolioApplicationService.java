package com.example.dzcom.application.service.portfolio;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.dzcom.application.assembler.portfolio.MockOrderExecutionViewAssembler;
import com.example.dzcom.application.assembler.portfolio.MockPortfolioViewAssembler;
import com.example.dzcom.application.command.portfolio.CreateMockPortfolioCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockBuyCommand;
import com.example.dzcom.application.command.portfolio.ExecuteMockPlanFromReportCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.portfolio.MockOrderExecutionView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioPerformanceView;
import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.dto.portfolio.PortfolioValuationView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.portfolio.MockOrder;
import com.example.dzcom.domain.model.portfolio.Portfolio;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.model.portfolio.Position;
import com.example.dzcom.domain.model.portfolio.TradeExecution;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductThemeRelation;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.portfolio.MockOrderStore;
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
import java.util.List;
import java.util.Set;

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
    private final TradeExecutionStore executions;
    private final ProductStore products;
    private final ProductInvestmentProfileStore investmentProfiles;
    private final ProductThemeRelationStore productThemeRelations;
    private final InvestmentAnalysisReportStore analysisReports;
    private final MarketQuoteStore quotes;
    private final MockPortfolioViewAssembler assembler;
    private final MockOrderExecutionViewAssembler executionAssembler;
    private final CurrentOperatorProvider currentOperator;
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
        String portfolioName = normalizeName(command.portfolioName());
        String baseCurrency = normalizeCurrency(command.baseCurrency());
        BigDecimal initialCash = normalizeInitialCash(command.initialCash());
        LocalDateTime now = clock.now();
        Portfolio portfolio = Portfolio.builder()
            .bizId(ids.newBizId())
            .portfolioNo(newPortfolioNo())
            .ownerUserBizId(operator.userBizId())
            .portfolioName(portfolioName)
            .portfolioType(PORTFOLIO_TYPE_SIMULATION)
            .baseCurrency(baseCurrency)
            .status(1)
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(operator.userBizId())
            .updatedBy(operator.userBizId())
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
        requireMockTradable(product.getBizId());
        BigDecimal amount = normalizeBuyAmount(command.amount());
        MarketQuote quote = quotes.findLatest(product.getBizId(), "1D", null)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "产品缺少最新行情，不能模拟成交"));
        BigDecimal price = quote.closePrice();
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品最新行情价格无效，不能模拟成交");
        }
        BigDecimal quantity = amount.divide(price, 8, RoundingMode.DOWN);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "买入金额不足以形成有效成交数量");
        }
        BigDecimal feeAmount = amount.multiply(defaultZero(product.getFeeRate()))
            .setScale(8, RoundingMode.HALF_UP);
        PortfolioValuation latest = valuations.findLatestByPortfolioBizId(portfolio.bizId())
            .orElseGet(() -> initialValuation(portfolio, BigDecimal.ZERO, clock.now()));
        BigDecimal requiredCash = amount.add(feeAmount);
        if (latest.cashBalance().compareTo(requiredCash) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "模拟组合现金不足");
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
        ensureReportExecutable(report);
        JSONObject plan = JSON.parseObject(report.investmentPlan());
        BigDecimal amount = plan.getBigDecimal("referenceAllocationAmount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报告未给出可执行的参考配置金额");
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
     * 计算单个持仓当前市值。
     *
     * @param position 当前持仓
     * @return 按最新行情计算的持仓市值
     * @throws BusinessException 当产品行情缺失或价格无效时抛出
     * @author dz
     * @date 2026-06-23
     */
    private BigDecimal currentPositionValue(Position position) {
        MarketQuote quote = quotes.findLatest(position.productBizId(), "1D", null)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.BAD_REQUEST,
                "持仓产品缺少最新行情，不能刷新估值"
            ));
        if (quote.closePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "持仓产品最新行情价格无效");
        }
        return position.quantity().multiply(quote.closePrice()).setScale(8, RoundingMode.HALF_UP);
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
    private void requireMockTradable(String productBizId) {
        boolean tradable = investmentProfiles.findByProductBizId(productBizId)
            .map(profile -> profile.mockTradable() && profile.dataQualityScore().compareTo(new BigDecimal("0.45")) >= 0)
            .orElse(false);
        if (!tradable) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品投资画像不足或未开启Mock交易");
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
    private void ensureReportExecutable(InvestmentAnalysisReport report) {
        if (!"SUCCESS".equals(report.status())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报告未成功生成，不能执行Mock交易");
        }
        if ("UNUSABLE".equals(report.confidenceLevel())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报告可信等级不可用，不能执行Mock交易");
        }
        if (report.dataQualityScore() == null
            || report.dataQualityScore().compareTo(new BigDecimal("0.45")) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报告数据质量不足，不能执行Mock交易");
        }
        JSONObject gate = JSON.parseObject(report.dataQualityGate());
        if (!gate.getBooleanValue("passed")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报告数据质量门禁未通过，不能执行Mock交易");
        }
        JSONObject plan = JSON.parseObject(report.investmentPlan());
        if ("DATA_GAP_REPORT".equals(plan.getString("planType"))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "数据缺口报告不能执行Mock交易");
        }
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

    /** 将空金额按 0 处理。 */
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
