package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** L1/L2 官方披露、交易所公告和理财产品净值专用采集任务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfficialDisclosureCollectionTaskHandler implements InvestmentTaskHandler {
    private static final Map<String, TaskProfile> PROFILES = Map.of(
        "REGULATORY_DISCLOSURE_COLLECTION", TaskProfile.builder()
            .defaultSourceCode("CSRC")
            .defaultArticleType("REGULATORY")
            .dataType("REGULATORY")
            .baseQualityScore(new BigDecimal("0.90"))
            .emptyMessage("监管披露端点未返回有效数据")
            .build(),
        "EXCHANGE_ANNOUNCEMENT_COLLECTION", TaskProfile.builder()
            .defaultSourceCode("CNINFO")
            .defaultArticleType("ANNOUNCEMENT")
            .dataType("ANNOUNCEMENT")
            .baseQualityScore(new BigDecimal("0.88"))
            .emptyMessage("交易所或巨潮公告端点未返回有效数据")
            .build(),
        "WEALTH_PRODUCT_NAV_REFRESH", TaskProfile.builder()
            .defaultSourceCode("CHINA_WEALTH")
            .defaultArticleType("WEALTH_NAV")
            .dataType("MARKET_QUOTE")
            .baseQualityScore(new BigDecimal("0.82"))
            .emptyMessage("理财产品或净值端点未返回有效数据")
            .build()
    );

    private final OfficialDisclosureClient client;
    private final NewsArticleStore articles;
    private final DataSourceStore sources;
    private final ProductStore products;
    private final MarketQuoteStore quotes;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /**
     * 判断当前处理器是否支持专用官方数据采集任务。
     *
     * @param taskType 任务类型
     * @return 支持监管披露、交易所公告、理财净值三类任务时返回 true
     * @author dz
     * @date 2026-06-24
     */
    @Override
    public boolean supports(String taskType) {
        return PROFILES.containsKey(taskType);
    }

    /**
     * 执行官方数据源专用采集。
     *
     * <p>该处理器只写入真实端点返回的数据，并同步刷新数据源健康和质量快照。
     * 未配置端点或端点无有效数据时，不写入任何兜底资讯，前端可通过数据源看板看到
     * 健康状态和数据质量缺口。</p>
     *
     * @param event 任务事件
     * @return 可审计执行摘要
     * @author dz
     * @date 2026-06-24
     */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        TaskProfile profile = PROFILES.get(event.taskType());
        String sourceCode = TaskParameterParser.string(event.parameters(), "sourceCode",
            profile.defaultSourceCode()).trim().toUpperCase(Locale.ROOT);
        String articleType = TaskParameterParser.string(event.parameters(), "articleType",
            profile.defaultArticleType()).trim().toUpperCase(Locale.ROOT);
        String languageCode = TaskParameterParser.string(event.parameters(), "languageCode", "zh-CN");
        int maxItems = TaskParameterParser.positiveInt(event.parameters(), "maxItems", 100);
        int timeoutSeconds = TaskParameterParser.positiveInt(event.parameters(), "timeoutSeconds", 20);
        int freshnessHours = TaskParameterParser.positiveInt(event.parameters(), "freshnessHours", 72);
        LocalDateTime now = clock.now();
        List<EndpointConfig> endpoints = resolveEndpoints(event);
        if (endpoints.isEmpty()) {
            saveHealth(sourceCode, false, 0, null, "未配置专用采集端点", now);
            return "未配置专用采集端点，未写入任何" + articleType + "数据";
        }

        FetchResult fetchResult = fetch(endpoints, event, maxItems, timeoutSeconds);
        List<OfficialDisclosureClient.DisclosureItem> uniqueItems = distinct(fetchResult.items()).stream()
            .limit(maxItems)
            .toList();
        uniqueItems.forEach(item -> saveArticle(item, sourceCode, articleType, languageCode, now));
        WealthSyncResult wealthSync = "WEALTH_PRODUCT_NAV_REFRESH".equals(event.taskType())
            ? syncWealthProductsAndNav(uniqueItems, event, sourceCode, now)
            : WealthSyncResult.empty();
        saveHealth(sourceCode, !uniqueItems.isEmpty(), uniqueItems.size(),
            fetchResult.averageLatencyMs(), failureReason(uniqueItems, fetchResult, profile), now);
        saveQualitySnapshot(sourceCode, profile, fetchResult.items(), uniqueItems,
            endpoints.size(), fetchResult.failedEndpoints(), freshnessHours, wealthSync, now);
        if (uniqueItems.isEmpty()) {
            return profile.emptyMessage() + "，未写入任何兜底数据";
        }
        return "已从 " + sourceCode + " 专用端点采集并保存 "
            + uniqueItems.size() + " 条" + articleType + "数据"
            + wealthSync.summarySuffix();
    }

    /** 逐个端点采集并隔离单端点异常。 */
    private FetchResult fetch(
        List<EndpointConfig> endpoints,
        InvestmentTaskEvent event,
        int maxItems,
        int timeoutSeconds
    ) {
        List<OfficialDisclosureClient.DisclosureItem> items = new ArrayList<>();
        int failedEndpoints = 0;
        long latency = 0;
        for (EndpointConfig endpoint : endpoints) {
            long started = System.nanoTime();
            try {
                items.addAll(client.fetch(request(endpoint, event, maxItems, timeoutSeconds)));
            } catch (RuntimeException exception) {
                failedEndpoints++;
                log.warn("官方专用端点采集失败: taskCode={}, endpointName={}, endpointUrl={}",
                    event.taskCode(), endpoint.name(), endpoint.url(), exception);
            } finally {
                latency += Duration.ofNanos(System.nanoTime() - started).toMillis();
            }
        }
        Integer averageLatency = endpoints.isEmpty()
            ? null
            : Math.toIntExact(Math.max(0, latency / endpoints.size()));
        return new FetchResult(items, failedEndpoints, averageLatency);
    }

    /** 构建端点采集请求。 */
    private OfficialDisclosureClient.DisclosureFetchRequest request(
        EndpointConfig endpoint,
        InvestmentTaskEvent event,
        int maxItems,
        int timeoutSeconds
    ) {
        return OfficialDisclosureClient.DisclosureFetchRequest.builder()
            .endpointName(endpoint.name())
            .endpointUrl(endpoint.url())
            .responseFormat(endpoint.format())
            .itemsPath(TaskParameterParser.string(event.parameters(), "itemsPath", ""))
            .externalIdPath(TaskParameterParser.string(event.parameters(), "externalIdPath", "id"))
            .titlePath(TaskParameterParser.string(event.parameters(), "titlePath", "title"))
            .summaryPath(TaskParameterParser.string(event.parameters(), "summaryPath", "summary"))
            .contentPath(TaskParameterParser.string(event.parameters(), "contentPath", "content"))
            .urlPath(TaskParameterParser.string(event.parameters(), "urlPath", "url"))
            .publishTimePath(TaskParameterParser.string(event.parameters(), "publishTimePath", "publishTime"))
            .extraFieldPaths(TaskParameterParser.string(event.parameters(), "extraFieldPaths", ""))
            .includeKeywords(TaskParameterParser.list(event.parameters(), "includeKeywords"))
            .maxItems(maxItems)
            .timeoutSeconds(timeoutSeconds)
            .build();
    }

    /** 解析任务配置中的端点集合。 */
    private List<EndpointConfig> resolveEndpoints(InvestmentTaskEvent event) {
        String raw = TaskParameterParser.string(event.parameters(), "endpoints", "");
        String defaultFormat = TaskParameterParser.string(event.parameters(), "responseFormat", "JSON");
        if (!raw.isBlank()) {
            List<EndpointConfig> parsed = new ArrayList<>();
            int index = 1;
            for (String item : raw.split(";")) {
                EndpointConfig endpoint = parseEndpoint(item, defaultFormat, index);
                if (endpoint != null) {
                    parsed.add(endpoint);
                    index++;
                }
            }
            return parsed;
        }
        List<String> urls = TaskParameterParser.list(event.parameters(), "endpointUrls");
        List<EndpointConfig> parsed = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            parsed.add(new EndpointConfig("endpoint-" + (i + 1), urls.get(i), normalizeFormat(defaultFormat)));
        }
        return parsed;
    }

    /** 解析单个“name=url|format”端点配置。 */
    private EndpointConfig parseEndpoint(String raw, String defaultFormat, int index) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] named = raw.trim().split("=", 2);
        String name = named.length == 2 ? named[0].trim() : "endpoint-" + index;
        String value = named.length == 2 ? named[1].trim() : named[0].trim();
        String[] parts = value.split("\\|", 2);
        String url = parts[0].trim();
        if (url.isBlank()) {
            return null;
        }
        String format = parts.length == 2 ? parts[1].trim() : defaultFormat;
        return new EndpointConfig(name.isBlank() ? "endpoint-" + index : name, url, normalizeFormat(format));
    }

    /** 端点格式标准化。 */
    private String normalizeFormat(String value) {
        String format = value == null || value.isBlank() ? "JSON" : value.trim().toUpperCase(Locale.ROOT);
        return "HTML".equals(format) ? "HTML" : "JSON";
    }

    /** 根据来源和外部 ID 去重，保留原始顺序。 */
    private List<OfficialDisclosureClient.DisclosureItem> distinct(
        List<OfficialDisclosureClient.DisclosureItem> items
    ) {
        Set<String> seen = new LinkedHashSet<>();
        return items.stream()
            .filter(item -> item.title() != null && !item.title().isBlank())
            .filter(item -> seen.add(stableKey(item)))
            .toList();
    }

    /** 保存官方披露条目。 */
    private void saveArticle(
        OfficialDisclosureClient.DisclosureItem item,
        String sourceCode,
        String articleType,
        String languageCode,
        LocalDateTime collectedAt
    ) {
        LocalDateTime publishTime = item.publishTime() == null ? collectedAt : item.publishTime();
        NewsArticle article = NewsArticle.builder()
            .bizId(ids.newBizId())
            .externalId(limit(resolveExternalId(item, sourceCode), 128))
            .articleType(limit(articleType, 32))
            .title(limit(item.title(), 320))
            .summary(item.summary())
            .content(item.content())
            .sourceCode(sourceCode)
            .sourceUrl(limit(item.url(), 1024))
            .languageCode(languageCode)
            .publishTime(publishTime)
            .collectedAt(collectedAt)
            .createdAt(collectedAt)
            .build();
        articles.save(article);
    }

    /** 保存数据源健康状态。 */
    private void saveHealth(
        String sourceCode,
        boolean success,
        int sampleCount,
        Integer averageLatencyMs,
        String failureReason,
        LocalDateTime now
    ) {
        DataSourceHealth existing = sources.findHealthBySourceCode(sourceCode).orElse(null);
        DataSourceHealth health = DataSourceHealth.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .lastSuccessAt(success ? now : existing == null ? null : existing.lastSuccessAt())
            .lastFailureAt(success ? existing == null ? null : existing.lastFailureAt() : now)
            .successRate(success ? BigDecimal.ONE : BigDecimal.ZERO)
            .avgLatencyMs(averageLatencyMs)
            .failureReason(success ? null : limit(failureReason, 512))
            .sampleCount(sampleCount)
            .updatedAt(now)
            .build();
        sources.saveHealth(health);
    }

    /** 保存数据质量快照。 */
    private void saveQualitySnapshot(
        String sourceCode,
        TaskProfile profile,
        List<OfficialDisclosureClient.DisclosureItem> fetchedItems,
        List<OfficialDisclosureClient.DisclosureItem> uniqueItems,
        int endpointCount,
        int failedEndpoints,
        int freshnessHours,
        WealthSyncResult wealthSync,
        LocalDateTime now
    ) {
        BigDecimal duplicateRate = ratio(Math.max(fetchedItems.size() - uniqueItems.size(), 0),
            Math.max(fetchedItems.size(), 1));
        BigDecimal missingRate = uniqueItems.isEmpty()
            ? BigDecimal.ONE
            : ratio(failedEndpoints, Math.max(endpointCount, 1));
        BigDecimal freshnessScore = freshnessScore(uniqueItems, freshnessHours, now);
        BigDecimal qualityScore = uniqueItems.isEmpty()
            ? BigDecimal.ZERO
            : profile.baseQualityScore()
                .multiply(BigDecimal.ONE.subtract(missingRate).max(BigDecimal.ZERO))
                .multiply(BigDecimal.ONE.subtract(duplicateRate).max(BigDecimal.ZERO))
                .multiply(freshnessScore)
                .setScale(4, RoundingMode.HALF_UP);
        DataQualitySnapshot snapshot = DataQualitySnapshot.builder()
            .bizId(ids.newBizId())
            .sourceCode(sourceCode)
            .dataType(profile.dataType())
            .qualityScore(qualityScore)
            .missingRate(missingRate)
            .duplicateRate(duplicateRate)
            .freshnessScore(freshnessScore)
            .sampleCount(uniqueItems.size())
            .snapshotTime(now)
            .detail(qualityDetail(endpointCount, failedEndpoints, fetchedItems.size(), uniqueItems.size(), wealthSync))
            .createdAt(now)
            .build();
        sources.saveQualitySnapshot(snapshot);
    }

    /** 生成质量快照明细 JSON。 */
    private String qualityDetail(
        int endpointCount,
        int failedEndpoints,
        int fetchedCount,
        int uniqueCount,
        WealthSyncResult wealthSync
    ) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("collector", "OfficialDisclosureCollectionTaskHandler");
        detail.put("endpointCount", endpointCount);
        detail.put("failedEndpointCount", failedEndpoints);
        detail.put("fetchedCount", fetchedCount);
        detail.put("uniqueCount", uniqueCount);
        if (!wealthSync.isEmpty()) {
            detail.put("upsertedProductCount", wealthSync.productCount());
            detail.put("savedNavQuoteCount", wealthSync.quoteCount());
            detail.put("missingNavCount", wealthSync.missingNavCount());
        }
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    /** 计算条目新鲜度。 */
    private BigDecimal freshnessScore(
        List<OfficialDisclosureClient.DisclosureItem> items,
        int freshnessHours,
        LocalDateTime now
    ) {
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long fresh = items.stream()
            .filter(item -> item.publishTime() != null)
            .filter(item -> !item.publishTime().isBefore(now.minusHours(freshnessHours)))
            .count();
        if (fresh == 0) {
            return new BigDecimal("0.50");
        }
        return ratio((int) fresh, items.size());
    }

    /** 计算比例并限制到 0-1。 */
    private BigDecimal ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = BigDecimal.valueOf(numerator)
            .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
        return value.max(BigDecimal.ZERO).min(BigDecimal.ONE);
    }

    /** 解析失败摘要。 */
    private String failureReason(
        List<OfficialDisclosureClient.DisclosureItem> uniqueItems,
        FetchResult fetchResult,
        TaskProfile profile
    ) {
        if (!uniqueItems.isEmpty()) {
            return null;
        }
        return fetchResult.failedEndpoints() > 0
            ? "专用采集端点全部失败或无有效数据"
            : profile.emptyMessage();
    }

    /** 解析稳定外部 ID。 */
    private String resolveExternalId(OfficialDisclosureClient.DisclosureItem item, String sourceCode) {
        String raw = firstNonBlank(item.externalId(), item.url(), item.title());
        if (raw.length() <= 128) {
            return raw;
        }
        return sourceCode + "-" + UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    }

    /** 条目去重键。 */
    private String stableKey(OfficialDisclosureClient.DisclosureItem item) {
        return firstNonBlank(item.externalId(), item.url(), item.title()).toLowerCase(Locale.ROOT);
    }

    /** 获取第一个非空文本。 */
    private String firstNonBlank(String... values) {
        return Arrays.stream(values)
            .filter(value -> value != null && !value.isBlank())
            .findFirst()
            .orElse(UUID.randomUUID().toString());
    }

    /** 限制字段长度。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 同步理财产品主档和净值行情。 */
    private WealthSyncResult syncWealthProductsAndNav(
        List<OfficialDisclosureClient.DisclosureItem> items,
        InvestmentTaskEvent event,
        String sourceCode,
        LocalDateTime now
    ) {
        String marketCode = TaskParameterParser.string(event.parameters(), "productMarketCode", "BANK_WMP")
            .trim().toUpperCase(Locale.ROOT);
        String currency = TaskParameterParser.string(event.parameters(), "productCurrency", "CNY")
            .trim().toUpperCase(Locale.ROOT);
        String interval = TaskParameterParser.string(event.parameters(), "quoteInterval", "1D")
            .trim().toUpperCase(Locale.ROOT);
        int defaultRiskLevel = TaskParameterParser.positiveInt(event.parameters(), "defaultRiskLevel", 2);
        int productCount = 0;
        int quoteCount = 0;
        int missingNavCount = 0;
        for (OfficialDisclosureClient.DisclosureItem item : items) {
            WealthProductPayload payload = wealthPayload(item);
            if (payload.productCode().isBlank() || payload.productName().isBlank()) {
                continue;
            }
            Product product = upsertWealthProduct(payload, marketCode, currency, defaultRiskLevel, now);
            productCount++;
            BigDecimal nav = decimal(payload.nav());
            if (nav == null) {
                missingNavCount++;
                continue;
            }
            LocalDateTime quoteTime = item.publishTime() == null ? now : item.publishTime();
            quotes.savePoint(MarketQuote.builder()
                .bizId(ids.newBizId())
                .productBizId(product.getBizId())
                .sourceCode(sourceCode)
                .interval(interval)
                .quoteTime(quoteTime)
                .openPrice(nav)
                .highPrice(nav)
                .lowPrice(nav)
                .closePrice(nav)
                .previousClosePrice(decimal(payload.previousNav()))
                .volume(null)
                .turnoverAmount(decimal(payload.assetSize()))
                .status(QuoteStatus.VALID)
                .receivedAt(now)
                .createdAt(now)
                .build());
            quoteCount++;
        }
        return new WealthSyncResult(productCount, quoteCount, missingNavCount);
    }

    /** 新增或更新银行理财产品主档。 */
    private Product upsertWealthProduct(
        WealthProductPayload payload,
        String marketCode,
        String currency,
        int defaultRiskLevel,
        LocalDateTime now
    ) {
        Product existing = products.findByMarketAndCode(marketCode, payload.productCode()).orElse(null);
        if (existing == null) {
            Product product = Product.create(
                ids.newBizId(),
                "WMP" + UUID.nameUUIDFromBytes((marketCode + payload.productCode()).getBytes(StandardCharsets.UTF_8))
                    .toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT),
                payload.productCode(),
                payload.productName(),
                ProductType.BANK_WMP,
                marketCode,
                currency,
                resolveRiskLevel(payload.riskLevel(), defaultRiskLevel),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                payload.description(),
                "SYSTEM",
                now
            );
            return products.save(product);
        }
        existing.updateDetails(
            payload.productName(),
            resolveRiskLevel(payload.riskLevel(), existing.getRiskLevel()),
            existing.getMinInvestAmount(),
            existing.getAmountStep(),
            existing.getQuantityStep(),
            existing.getFeeRate(),
            existing.getListingDate(),
            existing.getDelistingDate(),
            payload.description(),
            "SYSTEM",
            now
        );
        if (existing.getTradeStatus() == ProductTradeStatus.DISABLED) {
            existing.changeTradeStatus(ProductTradeStatus.TRADABLE, "SYSTEM", now);
        }
        return products.save(existing);
    }

    /** 从披露条目提取理财产品字段。 */
    private WealthProductPayload wealthPayload(OfficialDisclosureClient.DisclosureItem item) {
        Map<String, String> extra = item.extraFields() == null ? Map.of() : item.extraFields();
        String productCode = firstNonBlank(extra.get("productCode"), item.externalId());
        String productName = firstNonBlank(extra.get("productName"), item.title());
        return new WealthProductPayload(
            productCode.trim().toUpperCase(Locale.ROOT),
            productName.trim(),
            firstNonBlank(extra.get("nav"), extra.get("netValue"), extra.get("unitNav")),
            firstNonBlank(extra.get("previousNav"), extra.get("previousNetValue")),
            extra.get("assetSize"),
            extra.get("riskLevel"),
            firstNonBlank(item.summary(), item.content())
        );
    }

    /** 解析风险等级。 */
    private int resolveRiskLevel(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return Math.max(1, Math.min(defaultValue, 5));
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
            .replace("PR", "")
            .replace("R", "")
            .replaceAll("[^0-9]", "");
        if (normalized.isBlank()) {
            return Math.max(1, Math.min(defaultValue, 5));
        }
        int parsed = Integer.parseInt(normalized);
        return Math.max(1, Math.min(parsed, 5));
    }

    /** 解析非负小数。 */
    private BigDecimal decimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(value.trim().replace(",", ""));
            return parsed.signum() < 0 ? null : parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /** 任务类型画像。 */
    @Builder
    private record TaskProfile(
        String defaultSourceCode,
        String defaultArticleType,
        String dataType,
        BigDecimal baseQualityScore,
        String emptyMessage
    ) {
    }

    /** 采集端点配置。 */
    private record EndpointConfig(String name, String url, String format) {
    }

    /** 端点采集结果。 */
    private record FetchResult(
        List<OfficialDisclosureClient.DisclosureItem> items,
        int failedEndpoints,
        Integer averageLatencyMs
    ) {
    }

    /** 理财产品字段载荷。 */
    private record WealthProductPayload(
        String productCode,
        String productName,
        String nav,
        String previousNav,
        String assetSize,
        String riskLevel,
        String description
    ) {
    }

    /** 理财产品和净值同步结果。 */
    private record WealthSyncResult(int productCount, int quoteCount, int missingNavCount) {
        static WealthSyncResult empty() {
            return new WealthSyncResult(0, 0, 0);
        }

        boolean isEmpty() {
            return productCount == 0 && quoteCount == 0 && missingNavCount == 0;
        }

        String summarySuffix() {
            if (isEmpty()) {
                return "";
            }
            return "，同步理财产品 " + productCount + " 个，保存净值行情 " + quoteCount + " 条";
        }
    }
}
