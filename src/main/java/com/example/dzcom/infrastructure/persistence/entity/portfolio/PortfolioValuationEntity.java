package com.example.dzcom.infrastructure.persistence.entity.portfolio;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟组合估值快照持久化实体。 */
@Schema(description = "模拟组合估值快照持久化实体")
@TableName("aiw_portfolio_valuation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioValuationEntity {
    /** 估值快照业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "估值快照业务唯一标识")
    private String bizId;
    /** 组合业务唯一标识。 */
    @Schema(description = "组合业务唯一标识")
    private String portfolioBizId;
    /** 估值时点（北京时间）。 */
    @Schema(description = "估值时点（北京时间）")
    private LocalDateTime valuationTime;
    /** 估值计价币种。 */
    @Schema(description = "估值计价币种")
    private String baseCurrency;
    /** 组合总资产。 */
    @Schema(description = "组合总资产")
    private BigDecimal totalAsset;
    /** 现金余额。 */
    @Schema(description = "现金余额")
    private BigDecimal cashBalance;
    /** 持仓市值。 */
    @Schema(description = "持仓市值")
    private BigDecimal positionValue;
    /** 持仓总成本。 */
    @Schema(description = "持仓总成本")
    private BigDecimal totalCost;
    /** 未实现盈亏。 */
    @Schema(description = "未实现盈亏")
    private BigDecimal unrealizedProfit;
    /** 已实现盈亏。 */
    @Schema(description = "已实现盈亏")
    private BigDecimal realizedProfit;
    /** 累计收益率。 */
    @Schema(description = "累计收益率")
    private BigDecimal totalReturnRate;
    /** 估值来源编码。 */
    @Schema(description = "估值来源编码")
    private String sourceCode;
    /** 记录创建时间（北京时间）。 */
    @Schema(description = "记录创建时间（北京时间）")
    private LocalDateTime createdAt;
}
