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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OHLCV 行情持久化实体。
 *
 * <p>产品、数据源、周期和行情时间构成业务唯一点。实体不关联产品 Entity，
 * 从而为后续把行情迁移到时序存储保留清晰边界。</p>
 */
@TableName("aiw_market_quote")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketQuoteEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String productBizId;
    private String sourceCode;
    private String quoteInterval;
    private LocalDateTime quoteTime;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal previousClosePrice;
    private BigDecimal volume;
    private BigDecimal turnoverAmount;
    private int quoteStatus;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
}
