package com.example.dzcom.interfaces.request.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 标准 OHLCV 行情点写入请求。 */
public record SaveMarketQuoteRequest(
    @NotBlank @Size(max = 64) String sourceCode,
    @NotBlank @Size(max = 16) String interval,
    @NotNull LocalDateTime quoteTime,
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal openPrice,
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal highPrice,
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal lowPrice,
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal closePrice,
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal previousClosePrice,
    @DecimalMin("0") @Digits(integer = 20, fraction = 8) BigDecimal volume,
    @DecimalMin("0") @Digits(integer = 20, fraction = 8) BigDecimal turnoverAmount,
    QuoteStatus status
) {
}
