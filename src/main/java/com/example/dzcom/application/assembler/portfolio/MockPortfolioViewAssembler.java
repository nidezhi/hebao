package com.example.dzcom.application.assembler.portfolio;

import com.example.dzcom.application.dto.portfolio.MockPortfolioView;
import com.example.dzcom.application.dto.portfolio.PortfolioValuationView;
import com.example.dzcom.application.dto.portfolio.PositionView;
import com.example.dzcom.domain.model.portfolio.Portfolio;
import com.example.dzcom.domain.model.portfolio.PortfolioValuation;
import com.example.dzcom.domain.model.portfolio.Position;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** 模拟组合应用视图组装器。 */
@Component
public class MockPortfolioViewAssembler {
    /**
     * 组装模拟组合列表项。
     *
     * @param portfolio 模拟组合领域对象
     * @param valuation 最新估值快照
     * @return 模拟组合列表视图
     * @author dz
     * @date 2026-06-23
     */
    public MockPortfolioView assembleSummary(
        Portfolio portfolio,
        Optional<PortfolioValuation> valuation
    ) {
        return assemble(portfolio, valuation, List.of());
    }

    /**
     * 组装模拟组合详情。
     *
     * @param portfolio 模拟组合领域对象
     * @param valuation 最新估值快照
     * @param positions 当前持仓集合
     * @return 模拟组合详情视图
     * @author dz
     * @date 2026-06-23
     */
    public MockPortfolioView assembleDetail(
        Portfolio portfolio,
        Optional<PortfolioValuation> valuation,
        List<Position> positions
    ) {
        return assemble(portfolio, valuation, positions);
    }

    /**
     * 按统一结构组装模拟组合视图。
     *
     * @param portfolio 模拟组合领域对象
     * @param valuation 最新估值快照
     * @param positions 当前持仓集合
     * @return 模拟组合视图
     * @author dz
     * @date 2026-06-23
     */
    private MockPortfolioView assemble(
        Portfolio portfolio,
        Optional<PortfolioValuation> valuation,
        List<Position> positions
    ) {
        return MockPortfolioView.builder()
            .bizId(portfolio.bizId())
            .portfolioNo(portfolio.portfolioNo())
            .ownerUserBizId(portfolio.ownerUserBizId())
            .portfolioName(portfolio.portfolioName())
            .portfolioType(portfolio.portfolioType())
            .baseCurrency(portfolio.baseCurrency())
            .status(portfolio.status())
            .latestValuation(valuation.map(this::toValuationView).orElse(null))
            .positions(positions.stream().map(this::toPositionView).toList())
            .createdAt(portfolio.createdAt())
            .updatedAt(portfolio.updatedAt())
            .build();
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
     * 转换持仓视图。
     *
     * @param position 持仓领域对象
     * @return 持仓视图
     * @author dz
     * @date 2026-06-23
     */
    private PositionView toPositionView(Position position) {
        return PositionView.builder()
            .bizId(position.bizId())
            .productBizId(position.productBizId())
            .positionSide(position.positionSide())
            .quantity(position.quantity())
            .availableQuantity(position.availableQuantity())
            .averageCost(position.averageCost())
            .costAmount(position.costAmount())
            .realizedProfit(position.realizedProfit())
            .lastTradeAt(position.lastTradeAt())
            .build();
    }
}
