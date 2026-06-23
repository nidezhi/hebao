package com.example.dzcom.interfaces.dto.response.portfolio;

import com.example.dzcom.application.dto.portfolio.MockPortfolioPerformanceView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** 模拟组合收益曲线响应。 */
@Builder
@Schema(description = "模拟组合收益曲线响应")
public record MockPortfolioPerformanceResponse(
    @Schema(description = "组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "最新累计收益率")
    BigDecimal latestReturnRate,
    @Schema(description = "最大回撤，小数形式")
    BigDecimal maxDrawdown,
    @Schema(description = "收益曲线点数量")
    int pointCount,
    @Schema(description = "估值曲线快照集合")
    List<PortfolioValuationResponse> valuations
) {
    /** 从应用层视图转换为接口响应。 */
    public static MockPortfolioPerformanceResponse from(MockPortfolioPerformanceView view) {
        return MockPortfolioPerformanceResponse.builder()
            .portfolioBizId(view.portfolioBizId())
            .latestReturnRate(view.latestReturnRate())
            .maxDrawdown(view.maxDrawdown())
            .pointCount(view.pointCount())
            .valuations(view.valuations().stream().map(PortfolioValuationResponse::from).toList())
            .build();
    }
}
