package com.example.dzcom.application.command.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** 保存产品投资画像和主题关系的应用命令。 */
@Builder
@Schema(description = "保存产品投资画像和主题关系应用命令")
public record SaveProductInvestmentProfileCommand(
    @Schema(description = "产品业务唯一标识")
    String productBizId,
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
    Integer suitableRiskLevel,
    @Schema(description = "是否允许进入 Mock 交易")
    Boolean mockTradable,
    @Schema(description = "建议最短持有天数")
    Integer minHoldingDays,
    @Schema(description = "交易约束和注意事项")
    String tradingNotes,
    @Schema(description = "画像数据质量分，0-1")
    BigDecimal dataQualityScore,
    @Schema(description = "产品主题、行业、指数和资产类别关系集合")
    List<Relation> relations
) {
    /** 产品主题关系命令项。 */
    @Builder
    @Schema(description = "产品主题关系命令项")
    public record Relation(
        @Schema(description = "关系类型：THEME/INDUSTRY/INDEX/ASSET_CLASS")
        String relationType,
        @Schema(description = "关系稳定编码")
        String relationCode,
        @Schema(description = "关系展示名称")
        String relationName,
        @Schema(description = "关系权重，0-1")
        BigDecimal relationWeight,
        @Schema(description = "关系来源编码")
        String sourceCode,
        @Schema(description = "关系证据摘要")
        String evidence
    ) {
    }
}
