package com.example.dzcom.infrastructure.persistence.entity.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "aiw_market_quote")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketQuoteEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "product_biz_id", nullable = false, length = 36)
    private String productBizId;
    @Column(name = "source_code", nullable = false, length = 64)
    private String sourceCode;
    @Column(name = "quote_interval", nullable = false, length = 16)
    private String quoteInterval;
    @Column(name = "quote_time", nullable = false)
    private LocalDateTime quoteTime;
    @Column(name = "open_price", precision = 24, scale = 8)
    private BigDecimal openPrice;
    @Column(name = "high_price", precision = 24, scale = 8)
    private BigDecimal highPrice;
    @Column(name = "low_price", precision = 24, scale = 8)
    private BigDecimal lowPrice;
    @Column(name = "close_price", nullable = false, precision = 24, scale = 8)
    private BigDecimal closePrice;
    @Column(name = "previous_close_price", precision = 24, scale = 8)
    private BigDecimal previousClosePrice;
    @Column(precision = 28, scale = 8)
    private BigDecimal volume;
    @Column(name = "turnover_amount", precision = 28, scale = 8)
    private BigDecimal turnoverAmount;
    @Column(name = "quote_status", nullable = false, columnDefinition = "TINYINT")
    private int quoteStatus;
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
