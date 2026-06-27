package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductInvestmentProfile;
import com.example.dzcom.domain.model.product.ProductThemeRelation;
import com.example.dzcom.domain.repository.product.ProductInvestmentProfileStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.product.ProductThemeRelationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 确定性真实产品池同步任务。 */
@Service
@RequiredArgsConstructor
public class RealProductUniverseSyncTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "REAL_PRODUCT_UNIVERSE_SYNC";

    private final RealMarketDataClient client;
    private final ProductStore products;
    private final ProductInvestmentProfileStore investmentProfiles;
    private final ProductThemeRelationStore relations;
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
        String sourceCode = sourceCode(parameters);
        String baseUrl = TaskParameterParser.string(parameters, "providerBaseUrl", "");
        List<String> productCodes = support.productCodes(parameters);
        support.ensureSource(sourceCode, "AKShare真实产品池", "MARKET", "L3", baseUrl,
            "DAILY", "确定性产品池同步，产品代码来自任务配置或授权数据源", now);

        List<RealMarketDataClient.ProductPayload> payloads = client.products(request(parameters, sourceCode, productCodes));
        int saved = 0;
        for (RealMarketDataClient.ProductPayload payload : payloads) {
            Product product = upsert(payload, parameters, now);
            upsertInvestmentProfile(product, payload, now);
            saveThemeRelations(product, parameters, sourceCode, now);
            saved++;
        }
        int missing = Math.max(productCodes.size() - saved, 0);
        support.saveHealth(sourceCode, saved, "产品池同步未返回有效产品", now);
        support.saveQuality(sourceCode, "PRODUCT", productCodes.size(), saved, missing, 0, BigDecimal.ONE,
            support.detail("taskCode", event.taskCode(), "expectedProductCount", productCodes.size(),
                "savedProductCount", saved, "missingProductCount", missing),
            now);
        return "真实产品池同步完成: expected=" + productCodes.size() + ", saved=" + saved + ", missing=" + missing;
    }

    private RealMarketDataClient.MarketDataRequest request(
        Map<String, String> parameters,
        String sourceCode,
        List<String> productCodes
    ) {
        return RealMarketDataClient.MarketDataRequest.builder()
            .providerBaseUrl(TaskParameterParser.string(parameters, "providerBaseUrl", ""))
            .sourceCode(sourceCode)
            .marketScope(TaskParameterParser.marketScope(parameters))
            .themes(TaskParameterParser.themes(parameters))
            .productCodes(productCodes)
            .keywords(List.of())
            .lookbackDays(TaskParameterParser.positiveInt(parameters, "lookbackDays", 10))
            .maxItems(TaskParameterParser.positiveInt(parameters, "maxItems", Math.max(productCodes.size(), 1)))
            .timeoutSeconds(TaskParameterParser.positiveInt(parameters, "timeoutSeconds", 12))
            .build();
    }

    private Product upsert(RealMarketDataClient.ProductPayload payload, Map<String, String> parameters, LocalDateTime now) {
        String marketCode = firstNonBlank(payload.marketCode(), inferMarketCode(payload.productCode()));
        String productCode = payload.productCode().trim().toUpperCase(Locale.ROOT);
        Product existing = products.findByMarketAndCode(marketCode, productCode).orElse(null);
        int riskLevel = payload.riskLevel() == null
            ? TaskParameterParser.positiveInt(parameters, "defaultRiskLevel", "STOCK".equals(payload.productType()) ? 4 : 3)
            : payload.riskLevel();
        String name = firstNonBlank(payload.productName(), productCode);
        String description = support.limit(firstNonBlank(payload.description(), "REAL_PRODUCT_UNIVERSE_SYNC"), 500);
        if (existing == null) {
            return products.save(Product.create(
                ids.newBizId(),
                productNo(marketCode, productCode),
                productCode,
                name,
                productType(payload.productType(), productCode),
                marketCode,
                firstNonBlank(payload.currency(), "CNY"),
                riskLevel,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                description,
                "REAL_PRODUCT_UNIVERSE_SYNC",
                now
            ));
        }
        existing.updateDetails(
            name,
            riskLevel,
            existing.getMinInvestAmount(),
            existing.getAmountStep(),
            existing.getQuantityStep(),
            existing.getFeeRate(),
            existing.getListingDate(),
            existing.getDelistingDate(),
            description,
            "REAL_PRODUCT_UNIVERSE_SYNC",
            now
        );
        return products.save(existing);
    }

    /** 为真实产品补齐保守投资画像，使 Mock 交易风控有明确依据。 */
    private void upsertInvestmentProfile(
        Product product,
        RealMarketDataClient.ProductPayload payload,
        LocalDateTime now
    ) {
        ProductInvestmentProfile existing = investmentProfiles.findByProductBizId(product.getBizId()).orElse(null);
        BigDecimal baselineQuality = new BigDecimal("0.65");
        investmentProfiles.save(ProductInvestmentProfile.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .productBizId(product.getBizId())
            .assetClass(assetClass(product))
            .riskSummary(riskSummary(product, payload))
            .volatilityLevel(volatilityLevel(product))
            .liquidityLevel("HIGH")
            .maxDrawdown(existing == null ? null : existing.maxDrawdown())
            .suitableRiskLevel(product.getRiskLevel())
            .mockTradable(existing == null || existing.mockTradable())
            .minHoldingDays(existing == null ? minHoldingDays(product) : existing.minHoldingDays())
            .tradingNotes(tradingNotes(product))
            .dataQualityScore(existing == null
                ? baselineQuality
                : existing.dataQualityScore().max(baselineQuality))
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .build());
    }

    private void saveThemeRelations(Product product, Map<String, String> parameters, String sourceCode, LocalDateTime now) {
        List<ProductThemeRelation> productRelations = TaskParameterParser.themes(parameters).entrySet().stream()
            .filter(entry -> entry.getValue().contains(product.getProductCode()))
            .map(entry -> ProductThemeRelation.builder()
                .bizId(ids.newBizId())
                .productBizId(product.getBizId())
                .relationType("THEME")
                .relationCode(TaskParameterParser.themeCode(entry.getKey()))
                .relationName(entry.getKey())
                .relationWeight(BigDecimal.ONE)
                .sourceCode(sourceCode)
                .evidence("任务配置主题标的")
                .createdAt(now)
                .updatedAt(now)
                .build())
            .toList();
        relations.replaceByProductBizId(product.getBizId(), productRelations);
    }

    private ProductType productType(String value, String code) {
        String type = firstNonBlank(value, inferProductType(code));
        return ProductType.valueOf(type.toUpperCase(Locale.ROOT));
    }

    private String assetClass(Product product) {
        if ("518880".equals(product.getProductCode()) || "159934".equals(product.getProductCode())) {
            return "GOLD";
        }
        return product.getProductType().name();
    }

    private String volatilityLevel(Product product) {
        if (product.getRiskLevel() >= 4) {
            return "HIGH";
        }
        if (product.getRiskLevel() <= 2) {
            return "LOW";
        }
        return "MEDIUM";
    }

    private int minHoldingDays(Product product) {
        return product.getRiskLevel() >= 4 ? 20 : 10;
    }

    private String riskSummary(Product product, RealMarketDataClient.ProductPayload payload) {
        String description = firstNonBlank(payload.description(), product.getDescription());
        return support.limit("真实采集产品画像，风险等级R" + product.getRiskLevel()
            + "，仅允许模拟交易和投研复盘使用。"
            + (description.isBlank() ? "" : " 数据说明：" + description), 512);
    }

    private String tradingNotes(Product product) {
        return support.limit("由真实产品池采集器生成基础画像；Mock成交必须依赖最新1D行情，禁止作为真实交易指令。产品代码="
            + product.getProductCode(), 1024);
    }

    private String productNo(String marketCode, String productCode) {
        return ("REAL" + UUID.nameUUIDFromBytes((marketCode + productCode).getBytes(StandardCharsets.UTF_8))
            .toString().replace("-", "")).substring(0, 24).toUpperCase(Locale.ROOT);
    }

    private String sourceCode(Map<String, String> parameters) {
        return TaskParameterParser.string(parameters, "sourceCode", "AKSHARE").trim().toUpperCase(Locale.ROOT);
    }

    private String inferMarketCode(String code) {
        return code.startsWith("51") || code.startsWith("58") || code.startsWith("60") || code.startsWith("68")
            ? "SSE" : "SZSE";
    }

    private String inferProductType(String code) {
        return code.startsWith("15") || code.startsWith("51") || code.startsWith("58") ? "ETF" : "STOCK";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
