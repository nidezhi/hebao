package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 确定性真实行情同步任务。 */
@Service
@RequiredArgsConstructor
public class RealMarketQuoteSyncTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "REAL_MARKET_QUOTE_SYNC";

    private final RealMarketDataClient client;
    private final ProductStore products;
    private final MarketQuoteStore quotes;
    private final RealDataTaskSupport support;
    private final IdGenerator ids;
    private final ClockProvider clock;

    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        Map<String, String> parameters = event.parameters() == null ? Map.of() : event.parameters();
        LocalDateTime now = clock.now();
        String sourceCode = TaskParameterParser.string(parameters, "sourceCode", "EASTMONEY_PUBLIC").trim().toUpperCase(Locale.ROOT);
        String baseUrl = TaskParameterParser.string(parameters, "providerBaseUrl", "");
        String interval = TaskParameterParser.string(parameters, "quoteInterval", "1D").trim().toUpperCase(Locale.ROOT);
        List<String> productCodes = support.productCodes(parameters);
        support.ensureSource(sourceCode, "东方财富公开行情", "MARKET", "L3", baseUrl,
            "AFTER_MARKET", "确定性行情同步，写入日线行情或净值；未配置providerBaseUrl时使用东方财富公开K线接口", now);

        List<RealMarketDataClient.QuotePayload> payloads = client.quotes(request(parameters, sourceCode, productCodes));
        int saved = 0;
        int missingProduct = 0;
        for (RealMarketDataClient.QuotePayload payload : payloads) {
            Product product = products.findByMarketAndCode(payload.marketCode(), payload.productCode()).orElse(null);
            if (product == null) {
                missingProduct++;
                continue;
            }
            quotes.savePoint(MarketQuote.builder()
                .bizId(ids.newBizId())
                .productBizId(product.getBizId())
                .sourceCode(sourceCode)
                .interval(interval)
                .quoteTime(payload.quoteTime())
                .openPrice(payload.openPrice())
                .highPrice(payload.highPrice())
                .lowPrice(payload.lowPrice())
                .closePrice(payload.closePrice())
                .previousClosePrice(payload.previousClosePrice())
                .volume(payload.volume())
                .turnoverAmount(payload.turnoverAmount())
                .status(QuoteStatus.VALID)
                .receivedAt(now)
                .createdAt(now)
                .build());
            saved++;
        }
        int expectedMin = productCodes.size() * 2;
        int missing = Math.max(expectedMin - saved, 0) + missingProduct;
        support.saveHealth(sourceCode, saved, "行情源未返回有效行情或产品池未同步", now);
        support.saveQuality(sourceCode, "MARKET_QUOTE", expectedMin, saved, missing, 0,
            saved > 0 ? BigDecimal.ONE : BigDecimal.ZERO,
            support.detail("taskCode", event.taskCode(), "expectedQuoteCount", expectedMin,
                "savedQuoteCount", saved, "missingProductCount", missingProduct),
            now);
        return "真实行情同步完成: expectedMin=" + expectedMin + ", saved=" + saved + ", missing=" + missing;
    }

    private RealMarketDataClient.MarketDataRequest request(
        Map<String, String> parameters,
        String sourceCode,
        List<String> productCodes
    ) {
        int lookbackDays = TaskParameterParser.positiveInt(parameters, "lookbackDays", 10);
        return RealMarketDataClient.MarketDataRequest.builder()
            .providerBaseUrl(TaskParameterParser.string(parameters, "providerBaseUrl", ""))
            .sourceCode(sourceCode)
            .marketScope(TaskParameterParser.marketScope(parameters))
            .themes(TaskParameterParser.themes(parameters))
            .productCodes(productCodes)
            .keywords(List.of())
            .lookbackDays(lookbackDays)
            .maxItems(TaskParameterParser.positiveInt(parameters, "maxItems", Math.max(productCodes.size() * lookbackDays, 1)))
            .timeoutSeconds(TaskParameterParser.positiveInt(parameters, "timeoutSeconds", 20))
            .build();
    }
}
