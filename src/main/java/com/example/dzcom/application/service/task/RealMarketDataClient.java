package com.example.dzcom.application.service.task;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 确定性真实行情与资讯数据访问端口。 */
public interface RealMarketDataClient {
    /** 查询产品基础信息。 */
    List<ProductPayload> products(MarketDataRequest request);

    /** 查询日线行情。 */
    List<QuotePayload> quotes(MarketDataRequest request);

    /** 查询主题资讯。 */
    List<NewsPayload> news(MarketDataRequest request);

    /** 真实数据采集请求。 */
    @Builder
    record MarketDataRequest(
        String providerBaseUrl,
        String sourceCode,
        String marketScope,
        Map<String, List<String>> themes,
        List<String> productCodes,
        List<String> keywords,
        int lookbackDays,
        int maxItems,
        int timeoutSeconds
    ) {
    }

    /** 产品基础信息。 */
    @Builder
    record ProductPayload(
        String productCode,
        String productName,
        String productType,
        String marketCode,
        String currency,
        Integer riskLevel,
        String description,
        String sourceUrl
    ) {
    }

    /** 行情点。 */
    @Builder
    record QuotePayload(
        String productCode,
        String marketCode,
        LocalDateTime quoteTime,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal previousClosePrice,
        BigDecimal volume,
        BigDecimal turnoverAmount,
        String sourceUrl
    ) {
    }

    /** 资讯条目。 */
    @Builder
    record NewsPayload(
        String externalId,
        String articleType,
        String title,
        String summary,
        String content,
        String sourceUrl,
        LocalDateTime publishTime,
        List<String> matchedKeywords
    ) {
    }
}
