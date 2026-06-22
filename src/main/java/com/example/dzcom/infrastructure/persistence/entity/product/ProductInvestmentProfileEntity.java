package com.example.dzcom.infrastructure.persistence.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 产品投资风险和交易画像持久化实体。 */
@Schema(description = "产品投资风险和交易画像持久化实体")
@TableName("aiw_product_investment_profile")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductInvestmentProfileEntity {
    /** 画像业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "画像业务唯一标识")
    private String bizId;
    /** 产品业务唯一标识。 */
    @Schema(description = "产品业务唯一标识")
    private String productBizId;
    /** 资产类别。 */
    @Schema(description = "资产类别：STOCK/ETF/FUND/BOND/BANK_WMP/GOLD/REIT 等")
    private String assetClass;
    /** 风险摘要。 */
    @Schema(description = "风险摘要")
    private String riskSummary;
    /** 波动等级。 */
    @Schema(description = "波动等级：LOW/MEDIUM/HIGH")
    private String volatilityLevel;
    /** 流动性等级。 */
    @Schema(description = "流动性等级：LOW/MEDIUM/HIGH")
    private String liquidityLevel;
    /** 最大回撤。 */
    @Schema(description = "历史或估算最大回撤，小数形式")
    private BigDecimal maxDrawdown;
    /** 适配用户风险等级。 */
    @Schema(description = "适配用户风险等级，1-5")
    private int suitableRiskLevel;
    /** 是否允许进入 Mock 交易。 */
    @Schema(description = "是否允许进入 Mock 交易")
    private boolean mockTradable;
    /** 建议最短持有天数。 */
    @Schema(description = "建议最短持有天数")
    private int minHoldingDays;
    /** 交易约束和注意事项。 */
    @Schema(description = "交易约束和注意事项")
    private String tradingNotes;
    /** 画像数据质量分。 */
    @Schema(description = "画像数据质量分，0-1")
    private BigDecimal dataQualityScore;
    /** 创建时间，北京时间。 */
    @Schema(description = "创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 更新时间，北京时间。 */
    @Schema(description = "更新时间，北京时间")
    private LocalDateTime updatedAt;
}
