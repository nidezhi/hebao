package com.example.dzcom.domain.model.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟组合估值快照领域对象。 */
@Builder
@Schema(description = "模拟组合估值快照领域对象")
public record PortfolioValuation(
    @Schema(description = "估值快照业务唯一标识")
    String bizId,
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
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
    String sourceCode,
    @Schema(description = "记录创建时间（北京时间）")
    LocalDateTime createdAt
) {
}
