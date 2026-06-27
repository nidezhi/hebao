package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 真实数据总质量快照任务。 */
@Service
@RequiredArgsConstructor
public class RealDataQualitySnapshotTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "REAL_DATA_QUALITY_SNAPSHOT";
    private static final String SOURCE_CODE = "REAL_DATA_GATE";

    private final ProductStore products;
    private final MarketQuoteStore quotes;
    private final NewsArticleStore articles;
    private final DataSourceStore sources;
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
        List<String> productCodes = support.productCodes(parameters);
        List<String> keywords = support.keywords(parameters);
        int freshnessHours = TaskParameterParser.positiveInt(parameters, "freshnessHours", 72);
        int minNewsCount = TaskParameterParser.positiveInt(parameters, "minNewsCount", 20);
        List<Product> activeProducts = products.search(new ProductSearchCriteria(
            null,
            null,
            ProductTradeStatus.TRADABLE,
            null,
            "CNY",
            1,
            500,
            "productCode",
            true
        )).items().stream()
            .filter(product -> productCodes.isEmpty() || productCodes.contains(product.getProductCode()))
            .toList();
        int productsReady = activeProducts.size();
        int quoteReady = (int) activeProducts.stream()
            .filter(product -> quotes.findHistory(product.getBizId(), "1D", null,
                now.minusDays(10), now.plusDays(1), 3).size() >= 2)
            .count();
        long recentNews = articles.countByKeywords(keywords, now.minusHours(freshnessHours));
        BigDecimal productCoverage = support.ratio(productsReady, Math.max(productCodes.size(), 1));
        BigDecimal quoteCoverage = support.ratio(quoteReady, Math.max(productCodes.size(), 1));
        BigDecimal newsCoverage = BigDecimal.valueOf(Math.min(recentNews, minNewsCount))
            .divide(BigDecimal.valueOf(minNewsCount), 4, RoundingMode.HALF_UP);
        BigDecimal quality = productCoverage.multiply(new BigDecimal("0.35"))
            .add(quoteCoverage.multiply(new BigDecimal("0.40")))
            .add(newsCoverage.multiply(new BigDecimal("0.25")))
            .setScale(4, RoundingMode.HALF_UP);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("taskCode", event.taskCode());
        detail.put("expectedProductCount", productCodes.size());
        detail.put("activeProductCount", productsReady);
        detail.put("quoteReadyProductCount", quoteReady);
        detail.put("recentNewsCount", recentNews);
        detail.put("minNewsCount", minNewsCount);
        detail.put("productCoverage", productCoverage);
        detail.put("quoteCoverage", quoteCoverage);
        detail.put("newsCoverage", newsCoverage);
        detail.put("reportAllowed", quality.compareTo(new BigDecimal("0.60")) >= 0);
        sources.saveQualitySnapshot(DataQualitySnapshot.builder()
            .bizId(ids.newBizId())
            .sourceCode(SOURCE_CODE)
            .dataType("CORE_DATA_GATE")
            .qualityScore(quality)
            .missingRate(BigDecimal.ONE.subtract(quality).max(BigDecimal.ZERO))
            .duplicateRate(BigDecimal.ZERO)
            .freshnessScore(newsCoverage)
            .sampleCount(productsReady + quoteReady + Math.toIntExact(Math.min(recentNews, Integer.MAX_VALUE)))
            .snapshotTime(now)
            .detail(support.json(detail))
            .createdAt(now)
            .build());
        support.saveHealth(SOURCE_CODE, quality.compareTo(new BigDecimal("0.60")) >= 0 ? 1 : 0,
            "真实核心数据质量未达到报告门禁", now);
        return "真实数据质量快照完成: quality=" + quality + ", products=" + productsReady
            + ", quoteReady=" + quoteReady + ", recentNews=" + recentNews;
    }
}
