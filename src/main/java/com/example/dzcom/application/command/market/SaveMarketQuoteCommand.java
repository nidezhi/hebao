package com.example.dzcom.application.command.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 行情采集或管理接口写入一个标准 OHLCV 行情点的用例输入。 */
@Builder
public record SaveMarketQuoteCommand(
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
    QuoteStatus status
) {
}
