package com.example.dzcom.application.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.AiJsonCompletionClient;
import com.example.dzcom.application.service.ai.AiModelBindingApplicationService;
import com.example.dzcom.application.service.ai.AiModelRuntimeConfigResolver;
import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.AiModelBinding;
import com.example.dzcom.domain.model.ai.AiSkill;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.domain.repository.ai.AiSkillStore;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 远程大模型结构化采集真实投资数据，并落库到资讯、产品和行情核心表。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiStructuredDataCollectionTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AI_STRUCTURED_DATA_COLLECTION";
    private static final String DEFAULT_SCENARIO = AiModelBindingApplicationService.DATA_SOURCE_DISCOVERY;
    private static final String DEFAULT_SKILL_CODE = "AI_STRUCTURED_DATA_COLLECTION_CORE";
    private static final Set<String> REJECTED_SOURCE_MARKERS = Set.of("MOCK", "FAKE", "FALLBACK", "DEMO", "TEST");
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    private final AiModelBindingApplicationService modelBindings;
    private final AiModelStore models;
    private final AiSkillStore skills;
    private final AiModelRuntimeConfigResolver modelRuntimeConfigs;
    private final List<AiJsonCompletionClient> aiJsonClients;
    private final NewsArticleStore articles;
    private final ProductStore products;
    private final MarketQuoteStore quotes;
    private final DataSourceStore sources;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 判断是否支持 AI 结构化真实数据采集任务。 */
    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    /** 执行远程模型采集、严格校验和核心数据落库。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        Map<String, String> parameters = event.parameters() == null ? Map.of() : event.parameters();
        LocalDateTime now = clock.now();
        String environment = TaskParameterParser.string(parameters, "environment",
            AiModelBindingApplicationService.DEFAULT_ENVIRONMENT);
        String scenarioCode = TaskParameterParser.string(parameters, "scenarioCode", DEFAULT_SCENARIO);
        String skillCode = TaskParameterParser.string(parameters, "skillCode", DEFAULT_SKILL_CODE);
        int maxNews = TaskParameterParser.positiveInt(parameters, "maxNews", 18);
        int maxProducts = TaskParameterParser.positiveInt(parameters, "maxProducts", 12);
        int maxQuotes = TaskParameterParser.positiveInt(parameters, "maxQuotes", 36);

        AiModelBinding binding = modelBindings.enabledBinding(scenarioCode, environment);
        AiModel model = models.findActiveByCode(binding.modelCode())
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                "AI结构化采集模型不存在或未启用: " + binding.modelCode()));
        AiSkill skill = skills.findActiveByCode(skillCode).orElse(null);
        AiModelRuntimeConfig runtimeConfig = modelRuntimeConfigs.resolve(model);
        if (runtimeConfig.mockEnabled()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI结构化采集要求远程模型，当前模型仍开启mockEnabled");
        }
        AiJsonCompletionClient client = aiJsonClients.stream()
            .filter(candidate -> candidate.supports(runtimeConfig.providerCode()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                "未找到AI结构化采集模型客户端: " + runtimeConfig.providerCode()));

        String userPrompt = userPrompt(event, parameters, maxNews, maxProducts, maxQuotes, now);
        log.info(
            "AI结构化数据采集模型调用开始: taskCode={}, eventId={}, scenarioCode={}, environment={}, modelCode={}, modelVersion={}, providerCode={}, skillCode={}, maxNews={}, maxProducts={}, maxQuotes={}",
            event.taskCode(), event.eventId(), binding.scenarioCode(), binding.environment(),
            runtimeConfig.modelCode(), runtimeConfig.modelVersion(), runtimeConfig.providerCode(),
            skill == null ? "INTERNAL_PROMPT" : skill.skillCode(), maxNews, maxProducts, maxQuotes
        );
        long started = System.nanoTime();
        String content = client.completeJson(
            "AI_STRUCTURED_DATA_COLLECTION",
            systemPrompt(skill),
            userPrompt,
            runtimeConfig
        );
        long durationMs = java.time.Duration.ofNanos(System.nanoTime() - started).toMillis();
        JSONObject root = parseRoot(content);
        CollectionResult result = persist(root, parameters, now, maxNews, maxProducts, maxQuotes);
        saveCollectionSource(result, now, durationMs);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("modelCode", runtimeConfig.modelCode());
        summary.put("modelVersion", runtimeConfig.modelVersion());
        summary.put("providerCode", runtimeConfig.providerCode());
        summary.put("skillCode", skill == null ? "INTERNAL_PROMPT" : skill.skillCode());
        summary.put("durationMs", durationMs);
        summary.put("savedNewsCount", result.newsCount());
        summary.put("upsertedProductCount", result.productCount());
        summary.put("savedQuoteCount", result.quoteCount());
        summary.put("rejectedCount", result.rejectedCount());
        summary.put("dataGaps", root.getJSONArray("dataGaps") == null ? List.of() : root.getJSONArray("dataGaps"));
        log.info("AI结构化数据采集完成: taskCode={}, eventId={}, summary={}",
            event.taskCode(), event.eventId(), JSON.toJSONString(summary));
        if (result.savedCount() == 0) {
            throw new InvestmentTaskBlockedException("AI结构化采集没有产生可追溯真实数据: " + JSON.toJSONString(summary));
        }
        return JSON.toJSONString(summary);
    }

    /** 生成系统提示词，优先使用数据库 Skill 指令。 */
    private String systemPrompt(AiSkill skill) {
        String skillInstruction = skill == null ? "" : skill.instructionContent();
        return """
            你是投资理财平台的真实数据结构化采集器。必须优先使用可追溯公开来源或授权数据源，不允许编造数据。
            如果当前模型或中转不具备联网/检索能力，必须返回空数组和 dataGaps，说明无法采集，不得凭记忆补全。
            所有 newsArticles、products、quotes 都必须包含 sourceCode 和 sourceUrl；sourceUrl 必须是原始可追溯网页或接口地址。
            输出必须是 JSON 对象，且只能包含：
            {
              "newsArticles": [],
              "products": [],
              "quotes": [],
              "dataGaps": []
            }
            newsArticles 字段：sourceCode, externalId, articleType, title, summary, content, sourceUrl, publishTime。
            products 字段：productCode, productName, productType, marketCode, currency, riskLevel, description, sourceCode, sourceUrl。
            quotes 字段：productCode, marketCode, sourceCode, interval, quoteTime, openPrice, highPrice, lowPrice, closePrice, previousClosePrice, volume, turnoverAmount, sourceUrl。
            """ + (skillInstruction == null || skillInstruction.isBlank() ? "" : "\nSkill指令：\n" + skillInstruction);
    }

    /** 生成用户提示词，明确主题产品代码和质量约束。 */
    private String userPrompt(
        InvestmentTaskEvent event,
        Map<String, String> parameters,
        int maxNews,
        int maxProducts,
        int maxQuotes,
        LocalDateTime now
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskCode", event.taskCode());
        payload.put("marketScope", TaskParameterParser.marketScope(parameters));
        payload.put("assetClass", TaskParameterParser.string(parameters, "assetClass", "MULTI_ASSET"));
        payload.put("dataTypes", TaskParameterParser.string(parameters, "dataTypes",
            "MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY"));
        payload.put("topicKeywords", TaskParameterParser.string(parameters, "topicKeywords",
            "AI人工智能,半导体,黄金,基金净值,ETF净值,监管政策,财经新闻"));
        payload.put("themes", TaskParameterParser.themes(parameters));
        payload.put("maxNews", maxNews);
        payload.put("maxProducts", maxProducts);
        payload.put("maxQuotes", maxQuotes);
        payload.put("freshnessRequirement", "优先采集最近72小时资讯；行情优先最近2个交易日或最近可用净值。");
        payload.put("traceabilityRequirement", "每条数据必须有sourceUrl；无法确认来源时不要输出该条。");
        payload.put("currentTimeAsiaShanghai", now);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("AI结构化采集提示词序列化失败", exception);
        }
    }

    /** 解析模型返回根对象。 */
    private JSONObject parseRoot(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "AI结构化采集模型返回为空");
        }
        try {
            return JSON.parseObject(content);
        } catch (RuntimeException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                "AI结构化采集模型输出JSON格式不合法: " + exception.getMessage());
        }
    }

    /** 持久化模型返回的三类核心数据。 */
    private CollectionResult persist(
        JSONObject root,
        Map<String, String> parameters,
        LocalDateTime now,
        int maxNews,
        int maxProducts,
        int maxQuotes
    ) {
        int rejected = 0;
        int newsCount = 0;
        int productCount = 0;
        int quoteCount = 0;
        JSONArray news = root.getJSONArray("newsArticles");
        if (news != null) {
            for (int index = 0; index < Math.min(news.size(), maxNews); index++) {
                JSONObject item = news.getJSONObject(index);
                if (saveNews(item, parameters, now)) {
                    newsCount++;
                } else {
                    rejected++;
                }
            }
        }
        JSONArray productArray = root.getJSONArray("products");
        if (productArray != null) {
            for (int index = 0; index < Math.min(productArray.size(), maxProducts); index++) {
                JSONObject item = productArray.getJSONObject(index);
                if (upsertProduct(item, now) != null) {
                    productCount++;
                } else {
                    rejected++;
                }
            }
        }
        JSONArray quoteArray = root.getJSONArray("quotes");
        if (quoteArray != null) {
            for (int index = 0; index < Math.min(quoteArray.size(), maxQuotes); index++) {
                JSONObject item = quoteArray.getJSONObject(index);
                if (saveQuote(item, now)) {
                    quoteCount++;
                } else {
                    rejected++;
                }
            }
        }
        return new CollectionResult(newsCount, productCount, quoteCount, rejected);
    }

    /** 保存单条资讯。 */
    private boolean saveNews(JSONObject item, Map<String, String> parameters, LocalDateTime now) {
        String sourceCode = sourceCode(item.getString("sourceCode"));
        String title = text(item.getString("title"));
        String sourceUrl = text(item.getString("sourceUrl"));
        LocalDateTime publishTime = dateTime(item.getString("publishTime"), null);
        if (sourceCode == null || title == null || !validSourceUrl(sourceUrl) || publishTime == null) {
            return false;
        }
        String topicKeywords = TaskParameterParser.string(parameters, "topicKeywords", "");
        String summary = text(item.getString("summary"));
        if (!topicKeywords.isBlank() && (summary == null || !containsAny(summary + title, topicKeywords))) {
            summary = (summary == null ? "" : summary + " ") + "主题关键词：" + topicKeywords;
        }
        articles.save(NewsArticle.builder()
            .bizId(ids.newBizId())
            .externalId(limit(firstNonBlank(item.getString("externalId"), sourceUrl, title), 128))
            .articleType(limit(firstNonBlank(item.getString("articleType"), "NEWS"), 32))
            .title(limit(title, 320))
            .summary(summary)
            .content(text(item.getString("content")))
            .sourceCode(sourceCode)
            .sourceUrl(limit(sourceUrl, 1024))
            .languageCode("zh-CN")
            .publishTime(publishTime)
            .collectedAt(now)
            .createdAt(now)
            .build());
        ensureSource(sourceCode, firstNonBlank(item.getString("sourceName"), sourceCode),
            firstNonBlank(item.getString("articleType"), "NEWS"), sourceUrl, now);
        return true;
    }

    /** 新增或更新产品。 */
    private Product upsertProduct(JSONObject item, LocalDateTime now) {
        String productCode = normalizeProductCode(item.getString("productCode"));
        String productName = text(item.getString("productName"));
        String marketCode = firstNonBlank(item.getString("marketCode"), "CN_FUND").trim().toUpperCase(Locale.ROOT);
        String sourceUrl = text(item.getString("sourceUrl"));
        if (productCode == null || productName == null || !validSourceUrl(sourceUrl)) {
            return null;
        }
        Product existing = products.findByMarketAndCode(marketCode, productCode).orElse(null);
        ProductType productType = productType(item.getString("productType"));
        String currency = firstNonBlank(item.getString("currency"), "CNY").trim().toUpperCase(Locale.ROOT);
        int riskLevel = riskLevel(item.getString("riskLevel"), existing == null ? 3 : existing.getRiskLevel());
        String description = firstNonBlank(item.getString("description"), sourceUrl);
        Product saved;
        if (existing == null) {
            saved = Product.create(
                ids.newBizId(),
                productNo(marketCode, productCode),
                productCode,
                productName,
                productType,
                marketCode,
                currency,
                riskLevel,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                description,
                "AI_STRUCTURED_COLLECTION",
                now
            );
        } else {
            existing.updateDetails(
                productName,
                riskLevel,
                existing.getMinInvestAmount(),
                existing.getAmountStep(),
                existing.getQuantityStep(),
                existing.getFeeRate(),
                existing.getListingDate(),
                existing.getDelistingDate(),
                description,
                "AI_STRUCTURED_COLLECTION",
                now
            );
            if (existing.getTradeStatus() == ProductTradeStatus.DISABLED) {
                existing.changeTradeStatus(ProductTradeStatus.TRADABLE, "AI_STRUCTURED_COLLECTION", now);
            }
            saved = existing;
        }
        Product result = products.save(saved);
        String sourceCode = sourceCode(item.getString("sourceCode"));
        if (sourceCode != null) {
            ensureSource(sourceCode, firstNonBlank(item.getString("sourceName"), sourceCode), "MARKET", sourceUrl, now);
        }
        return result;
    }

    /** 保存单条行情。 */
    private boolean saveQuote(JSONObject item, LocalDateTime now) {
        String productCode = normalizeProductCode(item.getString("productCode"));
        String marketCode = firstNonBlank(item.getString("marketCode"), "CN_FUND").trim().toUpperCase(Locale.ROOT);
        String sourceCode = sourceCode(item.getString("sourceCode"));
        String sourceUrl = text(item.getString("sourceUrl"));
        BigDecimal close = decimal(item.getString("closePrice"));
        LocalDateTime quoteTime = dateTime(item.getString("quoteTime"), now);
        if (productCode == null || sourceCode == null || !validSourceUrl(sourceUrl) || close == null) {
            return false;
        }
        Product product = products.findByMarketAndCode(marketCode, productCode)
            .orElseGet(() -> upsertProduct(item, now));
        if (product == null) {
            return false;
        }
        BigDecimal open = defaultPrice(decimal(item.getString("openPrice")), close);
        BigDecimal high = defaultPrice(decimal(item.getString("highPrice")), open.max(close));
        BigDecimal low = defaultPrice(decimal(item.getString("lowPrice")), open.min(close));
        quotes.savePoint(MarketQuote.builder()
            .bizId(ids.newBizId())
            .productBizId(product.getBizId())
            .sourceCode(sourceCode)
            .interval(firstNonBlank(item.getString("interval"), "1D").trim().toUpperCase(Locale.ROOT))
            .quoteTime(quoteTime)
            .openPrice(open)
            .highPrice(high)
            .lowPrice(low)
            .closePrice(close)
            .previousClosePrice(decimal(item.getString("previousClosePrice")))
            .volume(decimal(item.getString("volume")))
            .turnoverAmount(decimal(item.getString("turnoverAmount")))
            .status(QuoteStatus.VALID)
            .receivedAt(now)
            .createdAt(now)
            .build());
        ensureSource(sourceCode, firstNonBlank(item.getString("sourceName"), sourceCode), "MARKET", sourceUrl, now);
        return true;
    }

    /** 保存聚合健康和质量快照。 */
    private void saveCollectionSource(CollectionResult result, LocalDateTime now, long durationMs) {
        String sourceCode = "AI_STRUCTURED_COLLECTION";
        ensureSource(sourceCode, "AI结构化真实数据采集", "AI_COLLECTION", "https://model-configured-source.local", now);
        DataSourceHealth existing = sources.findHealthBySourceCode(sourceCode).orElse(null);
        boolean success = result.savedCount() > 0;
        sources.saveHealth(DataSourceHealth.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .lastSuccessAt(success ? now : existing == null ? null : existing.lastSuccessAt())
            .lastFailureAt(success ? existing == null ? null : existing.lastFailureAt() : now)
            .successRate(success ? BigDecimal.ONE : BigDecimal.ZERO)
            .avgLatencyMs(Math.toIntExact(Math.min(Integer.MAX_VALUE, durationMs)))
            .failureReason(success ? null : "模型未返回可追溯真实数据")
            .sampleCount(result.savedCount())
            .updatedAt(now)
            .build());
        saveQualitySnapshot(sourceCode, "NEWS", result.newsCount(), result.rejectedCount(), now);
        saveQualitySnapshot(sourceCode, "MARKET_QUOTE", result.quoteCount(), result.rejectedCount(), now);
    }

    /** 确保数据源注册存在。 */
    private void ensureSource(String sourceCode, String sourceName, String sourceType, String sourceUrl, LocalDateTime now) {
        if (sourceCode == null) {
            return;
        }
        DataSource existing = sources.findBySourceCode(sourceCode).orElse(null);
        sources.save(DataSource.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .sourceName(limit(firstNonBlank(sourceName, sourceCode), 128))
            .sourceType(limit(firstNonBlank(sourceType, "NEWS").toUpperCase(Locale.ROOT), 32))
            .trustLevel(existing == null ? "L3" : existing.trustLevel())
            .baseUrl(existing == null ? limit(baseUrl(sourceUrl), 512) : existing.baseUrl())
            .enabled(true)
            .fetchFrequency(existing == null ? "AI_STRUCTURED_ON_DEMAND" : existing.fetchFrequency())
            .owner(existing == null ? "AI_STRUCTURED_COLLECTION" : existing.owner())
            .description(existing == null ? "由远程大模型结构化采集结果自动登记；业务数据必须保留sourceUrl。"
                : existing.description())
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? "AI_STRUCTURED_COLLECTION" : existing.createdBy())
            .updatedBy("AI_STRUCTURED_COLLECTION")
            .build());
    }

    /** 保存质量快照。 */
    private void saveQualitySnapshot(String sourceCode, String dataType, int savedCount, int rejectedCount, LocalDateTime now) {
        int total = savedCount + rejectedCount;
        BigDecimal missingRate = total == 0
            ? BigDecimal.ONE
            : BigDecimal.valueOf(rejectedCount).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
        BigDecimal freshnessScore = savedCount == 0 ? BigDecimal.ZERO : BigDecimal.ONE;
        BigDecimal qualityScore = savedCount == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(0.86).multiply(BigDecimal.ONE.subtract(missingRate)).setScale(4, RoundingMode.HALF_UP);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("collector", "AiStructuredDataCollectionTaskHandler");
        detail.put("savedCount", savedCount);
        detail.put("rejectedCount", rejectedCount);
        detail.put("validationPolicy", "sourceUrl/publishTime/productCode/quotePrice required; mock/fallback sources rejected");
        sources.saveQualitySnapshot(DataQualitySnapshot.builder()
            .bizId(ids.newBizId())
            .sourceCode(sourceCode)
            .dataType(dataType)
            .qualityScore(qualityScore)
            .missingRate(missingRate)
            .duplicateRate(BigDecimal.ZERO)
            .freshnessScore(freshnessScore)
            .sampleCount(savedCount)
            .snapshotTime(now)
            .detail(writeJson(detail))
            .createdAt(now)
            .build());
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private boolean containsAny(String content, String keywords) {
        String normalized = content.toLowerCase(Locale.ROOT);
        return TaskParameterParser.list(Map.of("keywords", keywords), "keywords").stream()
            .anyMatch(keyword -> normalized.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    private String sourceCode(String value) {
        String code = text(value);
        if (code == null) {
            return null;
        }
        String normalized = code.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]+", "_");
        if (normalized.isBlank() || REJECTED_SOURCE_MARKERS.stream().anyMatch(normalized::contains)) {
            return null;
        }
        return limit(normalized, 64);
    }

    private boolean validSourceUrl(String value) {
        String url = text(value);
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    private String normalizeProductCode(String value) {
        String code = text(value);
        return code == null ? null : limit(code.trim().toUpperCase(Locale.ROOT), 64);
    }

    private ProductType productType(String value) {
        String normalized = firstNonBlank(value, "ETF").trim().toUpperCase(Locale.ROOT);
        if ("WMP".equals(normalized) || "WEALTH".equals(normalized) || "BANK_WEALTH".equals(normalized)) {
            return ProductType.BANK_WMP;
        }
        try {
            return ProductType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return ProductType.ETF;
        }
    }

    private int riskLevel(String value, int defaultValue) {
        String normalized = value == null ? "" : value.replaceAll("[^0-9]", "");
        if (normalized.isBlank()) {
            return Math.max(1, Math.min(defaultValue, 5));
        }
        return Math.max(1, Math.min(Integer.parseInt(normalized), 5));
    }

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

    private BigDecimal defaultPrice(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private LocalDateTime dateTime(String value, LocalDateTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String trimmed = value.trim();
        try {
            return OffsetDateTime.parse(trimmed).atZoneSameInstant(BEIJING_ZONE).toLocalDateTime();
        } catch (RuntimeException ignored) {
            // Continue with local date/time parsing.
        }
        try {
            return LocalDateTime.parse(trimmed.replace(' ', 'T'));
        } catch (RuntimeException ignored) {
            // Continue with date-only parsing.
        }
        try {
            return LocalDate.parse(trimmed).atStartOfDay();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private String productNo(String marketCode, String productCode) {
        String seed = marketCode + ":" + productCode;
        return "AI" + UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8))
            .toString().replace("-", "").substring(0, 22).toUpperCase(Locale.ROOT);
    }

    private String baseUrl(String sourceUrl) {
        if (!validSourceUrl(sourceUrl)) {
            return null;
        }
        try {
            java.net.URI uri = java.net.URI.create(sourceUrl);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (RuntimeException exception) {
            return sourceUrl;
        }
    }

    private String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 本次结构化采集入库结果。 */
    private record CollectionResult(int newsCount, int productCount, int quoteCount, int rejectedCount) {
        int savedCount() {
            return newsCount + productCount + quoteCount;
        }
    }
}
