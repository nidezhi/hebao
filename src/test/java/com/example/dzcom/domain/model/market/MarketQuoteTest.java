package com.example.dzcom.domain.model.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 行情价格区间与非负数约束测试。 */
class MarketQuoteTest {
    @Test
    void shouldCreateValidOhlcvPoint() {
        MarketQuote quote = quote("10.10", "10.50", "9.80", "10.30");

        assertEquals(new BigDecimal("10.30"), quote.closePrice());
        assertEquals(QuoteStatus.VALID, quote.status());
    }

    @Test
    void shouldRejectClosePriceOutsideHighLowRange() {
        assertThrows(IllegalArgumentException.class,
            () -> quote("10.10", "10.50", "9.80", "10.60"));
    }

    @Test
    void shouldRejectNegativeVolume() {
        assertThrows(IllegalArgumentException.class, () -> MarketQuote.builder()
            .bizId("quote-id")
            .productBizId("product-id")
            .sourceCode("TEST")
            .interval("1D")
            .quoteTime(LocalDateTime.of(2026, 6, 8, 8, 0))
            .closePrice(BigDecimal.TEN)
            .volume(new BigDecimal("-1"))
            .status(QuoteStatus.VALID)
            .receivedAt(LocalDateTime.of(2026, 6, 8, 8, 1))
            .createdAt(LocalDateTime.of(2026, 6, 8, 8, 1))
            .build());
    }

    private MarketQuote quote(String open, String high, String low, String close) {
        return MarketQuote.builder()
            .bizId("quote-id")
            .productBizId("product-id")
            .sourceCode("TEST")
            .interval("1D")
            .quoteTime(LocalDateTime.of(2026, 6, 8, 8, 0))
            .openPrice(new BigDecimal(open))
            .highPrice(new BigDecimal(high))
            .lowPrice(new BigDecimal(low))
            .closePrice(new BigDecimal(close))
            .volume(new BigDecimal("100"))
            .status(QuoteStatus.VALID)
            .receivedAt(LocalDateTime.of(2026, 6, 8, 8, 1))
            .createdAt(LocalDateTime.of(2026, 6, 8, 8, 1))
            .build();
    }
}
