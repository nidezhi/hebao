package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
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

/** 确定性真实资讯同步任务。 */
@Service
@RequiredArgsConstructor
public class RealNewsSyncTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "REAL_NEWS_SYNC";

    private final RealMarketDataClient client;
    private final NewsArticleStore articles;
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
        String sourceCode = TaskParameterParser.string(parameters, "sourceCode", "EASTMONEY_NEWS").trim().toUpperCase(Locale.ROOT);
        String baseUrl = TaskParameterParser.string(parameters, "providerBaseUrl", "");
        List<String> keywords = support.keywords(parameters);
        int maxItems = TaskParameterParser.positiveInt(parameters, "maxItems", 100);
        support.ensureSource(sourceCode, "东方财富公开资讯", "NEWS", "L3", baseUrl,
            "HOURLY", "确定性主题资讯同步；未配置providerBaseUrl时使用东方财富公开财经新闻页面", now);

        List<RealMarketDataClient.NewsPayload> payloads = client.news(request(parameters, sourceCode, keywords, maxItems));
        int saved = 0;
        for (RealMarketDataClient.NewsPayload payload : payloads) {
            LocalDateTime publishTime = payload.publishTime() == null ? now : payload.publishTime();
            articles.save(NewsArticle.builder()
                .bizId(ids.newBizId())
                .externalId(safeExternalId(payload, sourceCode))
                .articleType(firstNonBlank(payload.articleType(), "NEWS"))
                .title(support.limit(payload.title(), 320))
                .summary(payload.summary())
                .content(payload.content())
                .sourceCode(sourceCode)
                .sourceUrl(support.limit(payload.sourceUrl(), 1024))
                .languageCode("zh-CN")
                .publishTime(publishTime)
                .collectedAt(now)
                .createdAt(now)
                .build());
            saved++;
        }
        int expected = Math.min(maxItems, 20);
        int missing = Math.max(expected - saved, 0);
        support.saveHealth(sourceCode, saved, "资讯源未返回有效主题资讯", now);
        support.saveQuality(sourceCode, "NEWS", expected, saved, missing, 0,
            saved > 0 ? BigDecimal.ONE : BigDecimal.ZERO,
            support.detail("taskCode", event.taskCode(), "keywordCount", keywords.size(),
                "expectedNewsCount", expected, "savedNewsCount", saved),
            now);
        return "真实资讯同步完成: expected=" + expected + ", saved=" + saved + ", missing=" + missing;
    }

    private RealMarketDataClient.MarketDataRequest request(
        Map<String, String> parameters,
        String sourceCode,
        List<String> keywords,
        int maxItems
    ) {
        return RealMarketDataClient.MarketDataRequest.builder()
            .providerBaseUrl(TaskParameterParser.string(parameters, "providerBaseUrl", ""))
            .sourceCode(sourceCode)
            .marketScope(TaskParameterParser.marketScope(parameters))
            .themes(TaskParameterParser.themes(parameters))
            .productCodes(support.productCodes(parameters))
            .keywords(keywords)
            .lookbackDays(TaskParameterParser.positiveInt(parameters, "lookbackDays", 3))
            .maxItems(maxItems)
            .timeoutSeconds(TaskParameterParser.positiveInt(parameters, "timeoutSeconds", 20))
            .build();
    }

    private String safeExternalId(RealMarketDataClient.NewsPayload payload, String sourceCode) {
        String raw = firstNonBlank(payload.externalId(), payload.sourceUrl(), payload.title());
        if (raw.length() <= 128) {
            return raw;
        }
        return sourceCode + "-" + UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
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
