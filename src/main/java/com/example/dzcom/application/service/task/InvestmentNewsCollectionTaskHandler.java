package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** 投资资讯定时采集任务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentNewsCollectionTaskHandler implements InvestmentTaskHandler {
    private final InvestmentNewsFeedClient feedClient;
    private final NewsArticleStore articles;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 判断当前处理器是否支持指定任务类型。
     *
     * @param taskType Kafka 事件中的任务类型
     * @return 类型为 INVESTMENT_NEWS_COLLECTION 时返回 true
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public boolean supports(String taskType) {
        return "INVESTMENT_NEWS_COLLECTION".equals(taskType);
    }

    /**
     * 从配置的 RSS/Atom 源拉取资讯并幂等保存。
     *
     * <p>每个资讯源独立容错。所有外部源均无有效数据时，使用
     * {@code fallbackArticles} 保存中国大陆主题兜底资讯，保证后续热度汇总
     * 和投资分析报告具有新闻输入。</p>
     *
     * @param event 包含资讯源、来源编码、语言和兜底资讯的任务事件
     * @return 本次成功保存的资讯数量摘要
     * @author dz
     * @date 2026-06-18
     */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        List<String> feedUrls = TaskParameterParser.list(event.parameters(), "feedUrls");
        int maxItems = TaskParameterParser.positiveInt(event.parameters(), "maxItems", 50);
        String sourceCode = TaskParameterParser.string(event.parameters(), "sourceCode", "RSS");
        String languageCode = TaskParameterParser.string(event.parameters(), "languageCode", "zh-CN");
        LocalDateTime now = clock.now();

        List<InvestmentNewsFeedClient.FeedItem> feedItems = feedUrls.stream()
            .flatMap(feedUrl -> fetchFeedItems(event.taskCode(), feedUrl, maxItems).stream())
            .toList();
        if (!feedItems.isEmpty()) {
            feedItems.forEach(item ->
                saveFeedItem(item, sourceCode, languageCode, now));
            return "已采集并保存 " + feedItems.size() + " 条投资资讯";
        }

        List<FallbackArticle> fallbackArticles = parseFallbackArticles(event);
        fallbackArticles.forEach(article ->
            saveFallbackArticle(article, sourceCode, languageCode, now));
        return "已采集并保存 " + fallbackArticles.size() + " 条投资资讯";
    }

    /**
     * 拉取单个资讯源并将异常隔离在当前数据源。
     *
     * @param taskCode 当前任务编码，用于失败日志定位
     * @param feedUrl RSS 或 Atom 数据源地址
     * @param maxItems 单个数据源最多返回条数
     * @return 成功解析的资讯条目；数据源失败时返回空集合
     * @author dz
     * @date 2026-06-18
     */
    private List<InvestmentNewsFeedClient.FeedItem> fetchFeedItems(
        String taskCode,
        String feedUrl,
        int maxItems
    ) {
        try {
            return feedClient.fetch(feedUrl, maxItems).stream()
                .limit(maxItems)
                .toList();
        } catch (RuntimeException exception) {
            log.warn("投资资讯源采集失败: taskCode={}, url={}",
                taskCode, feedUrl, exception);
            return List.of();
        }
    }

    /**
     * 将外部资讯条目转换为领域对象并保存。
     *
     * @param item 已解析的外部资讯条目
     * @param sourceCode 数据来源稳定编码
     * @param languageCode 内容语言编码
     * @param collectedAt 本次采集时间，北京时间
     * @author dz
     * @date 2026-06-18
     */
    private void saveFeedItem(
        InvestmentNewsFeedClient.FeedItem item,
        String sourceCode,
        String languageCode,
        LocalDateTime collectedAt
    ) {
        String externalId = resolveExternalId(item.externalId(), item.title(), sourceCode);
        LocalDateTime publishTime = item.publishTime() == null
            ? collectedAt
            : item.publishTime();

        NewsArticle article = NewsArticle.builder()
            .bizId(ids.newBizId())
            .externalId(externalId)
            .articleType("NEWS")
            .title(item.title())
            .summary(item.summary())
            .content(item.content())
            .sourceCode(sourceCode)
            .sourceUrl(item.url())
            .languageCode(languageCode)
            .publishTime(publishTime)
            .collectedAt(collectedAt)
            .createdAt(collectedAt)
            .build();
        articles.save(article);
    }

    /**
     * 解析配置中的兜底资讯。
     *
     * <p>单条格式为 {@code 主题|标题|摘要}，多条使用分号分隔。
     * 格式不完整的条目会被过滤。</p>
     *
     * @param event 包含 fallbackArticles 参数的任务事件
     * @return 结构化兜底资讯列表
     * @author dz
     * @date 2026-06-18
     */
    private List<FallbackArticle> parseFallbackArticles(InvestmentTaskEvent event) {
        String fallback = TaskParameterParser.string(event.parameters(), "fallbackArticles", "");
        if (fallback.isBlank()) {
            return List.of();
        }
        return Arrays.stream(fallback.split(";"))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .map(item -> item.split("\\|", 3))
            .filter(parts -> parts.length == 3)
            .map(parts -> FallbackArticle.builder()
                .themeName(parts[0].trim())
                .title(parts[1].trim())
                .summary(parts[2].trim())
                .build())
            .filter(article -> !article.themeName().isBlank())
            .filter(article -> !article.title().isBlank())
            .filter(article -> !article.summary().isBlank())
            .toList();
    }

    /**
     * 保存一条结构化兜底资讯。
     *
     * @param fallbackArticle 配置解析后的兜底资讯
     * @param sourceCode 数据来源稳定编码
     * @param languageCode 内容语言编码
     * @param now 当前北京时间
     * @author dz
     * @date 2026-06-18
     */
    private void saveFallbackArticle(
        FallbackArticle fallbackArticle,
        String sourceCode,
        String languageCode,
        LocalDateTime now
    ) {
        String externalId = "fallback-"
            + TaskParameterParser.themeCode(fallbackArticle.themeName())
            + "-"
            + now.toLocalDate();
        NewsArticle article = NewsArticle.builder()
            .bizId(ids.newBizId())
            .externalId(externalId)
            .articleType("NEWS")
            .title(fallbackArticle.title())
            .summary(fallbackArticle.summary())
            .content(fallbackArticle.summary())
            .sourceCode(sourceCode)
            .sourceUrl(null)
            .languageCode(languageCode)
            .publishTime(now)
            .collectedAt(now)
            .createdAt(now)
            .build();
        articles.save(article);
    }

    /**
     * 解析外部内容 ID；数据源未提供 ID 时使用来源和标题构造稳定标识。
     *
     * @param externalId 数据源提供的原始内容 ID
     * @param title 资讯标题
     * @param sourceCode 数据来源稳定编码
     * @return 可用于幂等保存的外部内容 ID
     * @author dz
     * @date 2026-06-18
     */
    private String resolveExternalId(String externalId, String title, String sourceCode) {
        if (externalId != null && !externalId.isBlank()) {
            return externalId;
        }
        return (sourceCode + "-" + title).toLowerCase(Locale.ROOT);
    }

    /** 配置中的单条兜底资讯。 */
    @lombok.Builder
    private record FallbackArticle(
        String themeName,
        String title,
        String summary
    ) {
    }
}
