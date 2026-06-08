package com.example.dzcom.infrastructure.persistence.repository.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.infrastructure.persistence.entity.market.MarketQuoteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 行情仓储的 MySQL/JPA 实现。 */
@Repository
@RequiredArgsConstructor
public class MarketQuoteStoreAdapter implements MarketQuoteStore {
    private final JpaMarketQuoteRepository quotes;

    @Override
    public MarketQuote savePoint(MarketQuote value) {
        Optional<MarketQuoteEntity> existing = quotes
            .findByProductBizIdAndSourceCodeAndQuoteIntervalAndQuoteTime(
                value.productBizId(), value.sourceCode(), value.interval(), value.quoteTime());
        MarketQuoteEntity entity = existing
            .map(MarketQuoteEntity::toBuilder)
            .orElseGet(MarketQuoteEntity::builder)
            .bizId(existing.map(MarketQuoteEntity::getBizId).orElse(value.bizId()))
            .productBizId(value.productBizId())
            .sourceCode(value.sourceCode())
            .quoteInterval(value.interval())
            .quoteTime(value.quoteTime())
            .openPrice(value.openPrice())
            .highPrice(value.highPrice())
            .lowPrice(value.lowPrice())
            .closePrice(value.closePrice())
            .previousClosePrice(value.previousClosePrice())
            .volume(value.volume())
            .turnoverAmount(value.turnoverAmount())
            .quoteStatus(existing.isPresent() && value.status() == QuoteStatus.VALID
                ? QuoteStatus.CORRECTED.code() : value.status().code())
            .receivedAt(value.receivedAt())
            .createdAt(existing.map(MarketQuoteEntity::getCreatedAt).orElse(value.createdAt()))
            .build();
        return toDomain(quotes.save(entity));
    }

    @Override
    public Optional<MarketQuote> findLatest(String productBizId, String interval, String sourceCode) {
        return quotes.findLatest(productBizId, interval, sourceCode, PageRequest.of(0, 1))
            .stream().findFirst().map(this::toDomain);
    }

    @Override
    public List<MarketQuote> findHistory(String productBizId, String interval, String sourceCode,
                                         LocalDateTime from, LocalDateTime to, int limit) {
        return quotes.findHistory(productBizId, interval, sourceCode, from, to, PageRequest.of(0, limit))
            .stream().map(this::toDomain).toList();
    }

    private MarketQuote toDomain(MarketQuoteEntity entity) {
        return MarketQuote.builder()
            .bizId(entity.getBizId())
            .productBizId(entity.getProductBizId())
            .sourceCode(entity.getSourceCode())
            .interval(entity.getQuoteInterval())
            .quoteTime(entity.getQuoteTime())
            .openPrice(entity.getOpenPrice())
            .highPrice(entity.getHighPrice())
            .lowPrice(entity.getLowPrice())
            .closePrice(entity.getClosePrice())
            .previousClosePrice(entity.getPreviousClosePrice())
            .volume(entity.getVolume())
            .turnoverAmount(entity.getTurnoverAmount())
            .status(QuoteStatus.fromCode(entity.getQuoteStatus()))
            .receivedAt(entity.getReceivedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
