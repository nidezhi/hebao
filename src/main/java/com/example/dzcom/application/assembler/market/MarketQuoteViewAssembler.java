package com.example.dzcom.application.assembler.market;

import com.example.dzcom.application.dto.market.MarketQuoteView;
import com.example.dzcom.domain.model.market.MarketQuote;
import org.springframework.stereotype.Component;

/** 将行情领域对象转换为不包含持久化细节的接口视图。 */
@Component
public class MarketQuoteViewAssembler {
    public MarketQuoteView assemble(MarketQuote quote) {
        return MarketQuoteView.builder()
            .bizId(quote.bizId())
            .productBizId(quote.productBizId())
            .sourceCode(quote.sourceCode())
            .interval(quote.interval())
            .quoteTime(quote.quoteTime())
            .openPrice(quote.openPrice())
            .highPrice(quote.highPrice())
            .lowPrice(quote.lowPrice())
            .closePrice(quote.closePrice())
            .previousClosePrice(quote.previousClosePrice())
            .volume(quote.volume())
            .turnoverAmount(quote.turnoverAmount())
            .status(quote.status())
            .receivedAt(quote.receivedAt())
            .build();
    }
}
