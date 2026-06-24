package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.ThemeProductPerformance;
import com.example.dzcom.domain.repository.market.DataSourceSearchCriteria;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 官方披露专用采集任务测试。 */
class OfficialDisclosureCollectionTaskHandlerTest {

    /** 专用采集器应保存官方公告并同步数据源健康与质量快照。 */
    @Test
    void shouldSaveDisclosureAndQualitySnapshot() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);
        CapturingNewsArticleStore articles = new CapturingNewsArticleStore();
        CapturingDataSourceStore sources = new CapturingDataSourceStore();
        OfficialDisclosureCollectionTaskHandler handler = new OfficialDisclosureCollectionTaskHandler(
            request -> List.of(OfficialDisclosureClient.DisclosureItem.builder()
                .externalId("notice-1")
                .title("上市公司人工智能业务公告")
                .summary("公告摘要")
                .content("公告正文")
                .url("https://example.com/notice/1")
                .publishTime(now.minusHours(2))
                .build()),
            articles,
            sources,
            new CapturingProductStore(),
            new CapturingMarketQuoteStore(),
            new IncrementIdGenerator(),
            () -> now,
            JsonMapper.builder().findAndAddModules().build()
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("l1-exchange-announcement-collection")
            .taskType("EXCHANGE_ANNOUNCEMENT_COLLECTION")
            .parameters(Map.of(
                "sourceCode", "CNINFO",
                "articleType", "ANNOUNCEMENT",
                "endpoints", "cninfo=https://example.com/api/notices|JSON",
                "maxItems", "10"
            ))
            .build());

        assertTrue(result.contains("1 条ANNOUNCEMENT数据"));
        assertEquals(1, articles.saved.size());
        assertEquals("ANNOUNCEMENT", articles.saved.get(0).articleType());
        assertEquals("CNINFO", articles.saved.get(0).sourceCode());
        assertNotNull(sources.health);
        assertEquals(BigDecimal.ONE, sources.health.successRate());
        assertEquals(1, sources.snapshots.size());
        assertEquals("ANNOUNCEMENT", sources.snapshots.get(0).dataType());
        assertTrue(sources.snapshots.get(0).qualityScore().compareTo(BigDecimal.ZERO) > 0);
    }

    /** 未配置端点时不应写入任何伪造数据。 */
    @Test
    void shouldNotWriteFallbackWhenEndpointMissing() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);
        CapturingNewsArticleStore articles = new CapturingNewsArticleStore();
        CapturingDataSourceStore sources = new CapturingDataSourceStore();
        OfficialDisclosureCollectionTaskHandler handler = new OfficialDisclosureCollectionTaskHandler(
            request -> {
                throw new AssertionError("无端点时不应调用客户端");
            },
            articles,
            sources,
            new CapturingProductStore(),
            new CapturingMarketQuoteStore(),
            new IncrementIdGenerator(),
            () -> now,
            new ObjectMapper()
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("l1-regulatory-disclosure-collection")
            .taskType("REGULATORY_DISCLOSURE_COLLECTION")
            .parameters(Map.of("sourceCode", "CSRC"))
            .build());

        assertTrue(result.contains("未配置专用采集端点"));
        assertEquals(0, articles.saved.size());
        assertNotNull(sources.health);
        assertEquals(BigDecimal.ZERO, sources.health.successRate());
    }

    /** 理财净值任务应 upsert 产品池并写入 1D 净值行情。 */
    @Test
    void shouldUpsertWealthProductAndSaveNavQuote() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);
        CapturingNewsArticleStore articles = new CapturingNewsArticleStore();
        CapturingDataSourceStore sources = new CapturingDataSourceStore();
        CapturingProductStore products = new CapturingProductStore();
        CapturingMarketQuoteStore quotes = new CapturingMarketQuoteStore();
        OfficialDisclosureCollectionTaskHandler handler = new OfficialDisclosureCollectionTaskHandler(
            request -> List.of(OfficialDisclosureClient.DisclosureItem.builder()
                .externalId("wmp-001")
                .title("稳健理财一号")
                .summary("理财净值披露")
                .content("理财净值披露")
                .url("https://example.com/wmp/001")
                .publishTime(now.minusHours(1))
                .extraFields(Map.of(
                    "productCode", "wmp-001",
                    "productName", "稳健理财一号",
                    "nav", "1.0234",
                    "previousNav", "1.0200",
                    "assetSize", "1000000",
                    "riskLevel", "R2"
                ))
                .build()),
            articles,
            sources,
            products,
            quotes,
            new IncrementIdGenerator(),
            () -> now,
            JsonMapper.builder().findAndAddModules().build()
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("l2-wealth-product-nav-refresh")
            .taskType("WEALTH_PRODUCT_NAV_REFRESH")
            .parameters(Map.of(
                "sourceCode", "CHINA_WEALTH",
                "articleType", "WEALTH_NAV",
                "endpoints", "wealth=https://example.com/api/wmp|JSON",
                "productMarketCode", "BANK_WMP",
                "quoteInterval", "1D"
            ))
            .build());

        assertTrue(result.contains("同步理财产品 1 个"));
        assertEquals(1, products.saved.size());
        assertEquals("WMP-001", products.saved.get(0).getProductCode());
        assertEquals("BANK_WMP", products.saved.get(0).getMarketCode());
        assertEquals(1, quotes.saved.size());
        assertEquals(new BigDecimal("1.0234"), quotes.saved.get(0).closePrice());
        assertEquals("CHINA_WEALTH", quotes.saved.get(0).sourceCode());
        assertTrue(sources.snapshots.get(0).detail().contains("savedNavQuoteCount"));
    }

    /** 捕获资讯保存的仓储假实现。 */
    private static final class CapturingNewsArticleStore implements NewsArticleStore {
        private final List<NewsArticle> saved = new ArrayList<>();

        @Override
        public NewsArticle save(NewsArticle article) {
            saved.add(article);
            return article;
        }

        @Override
        public long countByKeywords(List<String> keywords, LocalDateTime from) {
            return 0;
        }

        @Override
        public List<NewsArticle> findRecentByKeywords(List<String> keywords, LocalDateTime from, int limit) {
            return List.of();
        }

        @Override
        public PageResult<NewsArticle> search(NewsArticleSearchCriteria criteria) {
            return PageResult.<NewsArticle>builder()
                .items(saved)
                .total(saved.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(saved.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 捕获产品保存的仓储假实现。 */
    private static final class CapturingProductStore implements ProductStore {
        private final List<Product> saved = new ArrayList<>();

        @Override
        public Product save(Product product) {
            saved.add(product);
            return product;
        }

        @Override
        public Optional<Product> findByBizId(String bizId) {
            return saved.stream().filter(product -> product.getBizId().equals(bizId)).findFirst();
        }

        @Override
        public boolean existsByMarketAndCode(String marketCode, String productCode) {
            return findByMarketAndCode(marketCode, productCode).isPresent();
        }

        @Override
        public Optional<Product> findByMarketAndCode(String marketCode, String productCode) {
            return saved.stream()
                .filter(product -> product.getMarketCode().equals(marketCode))
                .filter(product -> product.getProductCode().equals(productCode))
                .findFirst();
        }

        @Override
        public PageResult<Product> search(ProductSearchCriteria criteria) {
            return PageResult.<Product>builder()
                .items(saved)
                .total(saved.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(saved.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 捕获行情保存的仓储假实现。 */
    private static final class CapturingMarketQuoteStore implements MarketQuoteStore {
        private final List<MarketQuote> saved = new ArrayList<>();

        @Override
        public MarketQuote savePoint(MarketQuote quote) {
            saved.add(quote);
            return quote;
        }

        @Override
        public Optional<MarketQuote> findLatest(String productBizId, String interval, String sourceCode) {
            return saved.stream().findFirst();
        }

        @Override
        public List<MarketQuote> findHistory(
            String productBizId,
            String interval,
            String sourceCode,
            LocalDateTime from,
            LocalDateTime to,
            int limit
        ) {
            return saved;
        }

        @Override
        public List<ThemeProductPerformance> findPerformance(
            List<String> productCodes,
            LocalDateTime from,
            LocalDateTime to
        ) {
            return List.of();
        }
    }

    /** 捕获数据源健康和质量快照的仓储假实现。 */
    private static final class CapturingDataSourceStore implements DataSourceStore {
        private DataSourceHealth health;
        private final List<DataQualitySnapshot> snapshots = new ArrayList<>();

        @Override
        public DataSource save(DataSource source) {
            return source;
        }

        @Override
        public DataSourceHealth saveHealth(DataSourceHealth health) {
            this.health = health;
            return health;
        }

        @Override
        public DataQualitySnapshot saveQualitySnapshot(DataQualitySnapshot snapshot) {
            snapshots.add(snapshot);
            return snapshot;
        }

        @Override
        public Optional<DataSource> findBySourceCode(String sourceCode) {
            return Optional.empty();
        }

        @Override
        public Optional<DataSourceHealth> findHealthBySourceCode(String sourceCode) {
            return Optional.empty();
        }

        @Override
        public List<DataQualitySnapshot> findQualitySnapshots(String sourceCode, String dataType, int limit) {
            return List.of();
        }

        @Override
        public PageResult<DataSource> search(DataSourceSearchCriteria criteria) {
            return PageResult.<DataSource>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }
    }

    /** 递增业务 ID 生成器。 */
    private static final class IncrementIdGenerator implements IdGenerator {
        private int current = 1;

        @Override
        public String newBizId() {
            String value = "id-" + current;
            current++;
            return value;
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }
}
