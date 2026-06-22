package com.example.dzcom.domain.model.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 产品投资风险和交易画像领域对象。 */
@Builder
@Schema(description = "产品投资风险和交易画像领域对象")
public record ProductInvestmentProfile(
    @Schema(description = "画像业务唯一标识")
    String bizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "资产类别：STOCK/ETF/FUND/BOND/BANK_WMP/GOLD/REIT 等")
    String assetClass,
    @Schema(description = "风险摘要，前端产品详情页展示")
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
    BigDecimal dataQualityScore,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "记录更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
