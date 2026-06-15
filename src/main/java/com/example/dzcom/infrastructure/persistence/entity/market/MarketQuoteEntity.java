package com.example.dzcom.infrastructure.persistence.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OHLCV 行情持久化实体。
 *
 * <p>产品、数据源、周期和行情时间构成业务唯一点。实体不关联产品 Entity，
 * 从而为后续把行情迁移到时序存储保留清晰边界。</p>
 */
@Schema(description = "持久化：OHLCV 行情表（aiw_market_quote）")
@TableName("aiw_market_quote")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketQuoteEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    @Schema(description = "产品业务标识")
    private String productBizId;
    @Schema(description = "行情数据源编码")
    private String sourceCode;
    @Schema(description = "行情周期")
    private String quoteInterval;
    @Schema(description = "行情时间（UTC）")
    private LocalDateTime quoteTime;
    @Schema(description = "开盘价")
    private BigDecimal openPrice;
    @Schema(description = "最高价")
    private BigDecimal highPrice;
    @Schema(description = "最低价")
    private BigDecimal lowPrice;
    @Schema(description = "收盘价")
    private BigDecimal closePrice;
    @Schema(description = "前一周期收盘价")
    private BigDecimal previousClosePrice;
    @Schema(description = "成交量")
    private BigDecimal volume;
    @Schema(description = "成交额")
    private BigDecimal turnoverAmount;
    @Schema(description = "行情状态编码")
    private int quoteStatus;
    @Schema(description = "接收时间（UTC）")
    private LocalDateTime receivedAt;
    @Schema(description = "记录创建时间（UTC）")
    private LocalDateTime createdAt;
}
