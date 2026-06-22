package com.example.dzcom.interfaces.dto.response.product;

import com.example.dzcom.application.dto.product.ProductInvestmentProfileView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 产品投资风险和交易画像响应。 */
@Builder
@Schema(description = "产品投资风险和交易画像响应")
public record ProductInvestmentProfileResponse(
    @Schema(description = "资产类别：STOCK/ETF/FUND/BOND/BANK_WMP/GOLD/REIT 等")
    String assetClass,
    @Schema(description = "风险摘要")
    String riskSummary,
    @Schema(description = "波动等级：LOW/MEDIUM/HIGH")
    String volatilityLevel,
    @Schema(description = "流动性等级：LOW/MEDIUM/HIGH")
    String liquidityLevel,
    @Schema(description = "历史或估算最大回撤，小数形式")
    BigDecimal maxDrawdown,
    @Schema(description = "适配用户风险等级，1-5")
    int suitableRiskLevel,
    @Schema(description = "是否允许进入 Mock 交易")
    boolean mockTradable,
    @Schema(description = "建议最短持有天数")
    int minHoldingDays,
    @Schema(description = "交易约束和注意事项")
    String tradingNotes,
    @Schema(description = "画像数据质量分，0-1")
    BigDecimal dataQualityScore
) {
    /**
     * 将应用视图转换为接口响应。
     *
     * @param view 产品投资画像应用视图
     * @return 产品投资画像响应
     * @author dz
     * @date 2026-06-22
     */
    public static ProductInvestmentProfileResponse from(ProductInvestmentProfileView view) {
        if (view == null) {
            return null;
        }
        return ProductInvestmentProfileResponse.builder()
            .assetClass(view.assetClass())
            .riskSummary(view.riskSummary())
            .volatilityLevel(view.volatilityLevel())
            .liquidityLevel(view.liquidityLevel())
            .maxDrawdown(view.maxDrawdown())
            .suitableRiskLevel(view.suitableRiskLevel())
            .mockTradable(view.mockTradable())
            .minHoldingDays(view.minHoldingDays())
            .tradingNotes(view.tradingNotes())
            .dataQualityScore(view.dataQualityScore())
            .build();
    }
}
