package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.PortfolioValuationView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟组合估值快照响应。 */
@Builder
@Schema(description = "模拟组合估值快照响应")
public record PortfolioValuationResponse(
    @Schema(description = "估值时点（北京时间）")
    LocalDateTime valuationTime,
    @Schema(description = "估值计价币种")
    String baseCurrency,
    @Schema(description = "组合总资产")
    BigDecimal totalAsset,
    @Schema(description = "现金余额")
    BigDecimal cashBalance,
    @Schema(description = "持仓市值")
    BigDecimal positionValue,
    @Schema(description = "持仓总成本")
    BigDecimal totalCost,
    @Schema(description = "未实现盈亏")
    BigDecimal unrealizedProfit,
    @Schema(description = "已实现盈亏")
    BigDecimal realizedProfit,
    @Schema(description = "累计收益率")
    BigDecimal totalReturnRate,
    @Schema(description = "估值来源编码")
    String sourceCode
) {
    /**
     * 从应用层视图转换为接口响应。
     *
     * @param view 估值快照应用层视图
     * @return 估值快照响应
     * @author dz
     * @date 2026-06-23
     */
    public static PortfolioValuationResponse from(PortfolioValuationView view) {
        if (view == null) {
            return null;
        }
        return PortfolioValuationResponse.builder()
            .valuationTime(view.valuationTime())
            .baseCurrency(view.baseCurrency())
            .totalAsset(view.totalAsset())
            .cashBalance(view.cashBalance())
            .positionValue(view.positionValue())
            .totalCost(view.totalCost())
            .unrealizedProfit(view.unrealizedProfit())
            .realizedProfit(view.realizedProfit())
            .totalReturnRate(view.totalReturnRate())
            .sourceCode(view.sourceCode())
            .build();
    }
}
