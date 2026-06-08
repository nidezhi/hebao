package com.example.dzcom.domain.repository.market;

import com.example.dzcom.domain.model.market.MarketQuote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 市场行情仓储端口。
 */
public interface MarketQuoteStore {
    /**
     * 保存或修正唯一行情点。
     *
     * <p>唯一键是产品、数据源、周期和行情时间；重复采集不得产生两条互相矛盾的数据。</p>
     */
    MarketQuote savePoint(MarketQuote quote);

    Optional<MarketQuote> findLatest(String productBizId, String interval, String sourceCode);

    List<MarketQuote> findHistory(String productBizId, String interval, String sourceCode,
                                  LocalDateTime from, LocalDateTime to, int limit);
}
