package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** 模拟组合收益曲线应用层视图。 */
@Builder
@Schema(description = "模拟组合收益曲线应用层视图")
public record MockPortfolioPerformanceView(
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "最新累计收益率")
    BigDecimal latestReturnRate,
    @Schema(description = "最大回撤，小数形式")
    BigDecimal maxDrawdown,
    @Schema(description = "收益曲线点数量")
    int pointCount,
    @Schema(description = "估值曲线快照集合")
    List<PortfolioValuationView> valuations
) {
}
