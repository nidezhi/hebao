package com.example.dzcom.application.dto.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 面向 API 的标准行情视图。 */
@Builder
public record MarketQuoteView(
    String bizId,
    String productBizId,
    String sourceCode,
    String interval,
    LocalDateTime quoteTime,
    BigDecimal openPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal closePrice,
    BigDecimal previousClosePrice,
    BigDecimal volume,
    BigDecimal turnoverAmount,
    QuoteStatus status,
    LocalDateTime receivedAt
) {
}
