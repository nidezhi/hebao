package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 投资资讯定时采集任务。 */
@Service
@RequiredArgsConstructor
public class InvestmentNewsCollectionTaskHandler implements InvestmentTaskHandler {
    private final InvestmentNewsFeedClient feedClient;
    private final NewsArticleStore articles;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 支持投资资讯采集任务。 */
    @Override
    public boolean supports(String taskType) {
        return "INVESTMENT_NEWS_COLLECTION".equals(taskType);
    }

    /** 从配置的 RSS/Atom 源拉取并幂等保存资讯。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        List<String> feedUrls = TaskParameterParser.list(event.parameters(), "feedUrls");
        int maxItems = TaskParameterParser.positiveInt(event.parameters(), "maxItems", 50);
        String sourceCode = event.parameters().getOrDefault("sourceCode", "RSS");
        String languageCode = event.parameters().getOrDefault("languageCode", "zh-CN");
        LocalDateTime now = clock.now();
        long saved = feedUrls.stream()
            .flatMap(url -> feedClient.fetch(url, maxItems).stream())
            .limit((long) maxItems * Math.max(feedUrls.size(), 1))
            .map(item -> articles.save(NewsArticle.builder()
                .bizId(ids.newBizId())
                .externalId(item.externalId())
                .articleType("NEWS")
                .title(item.title())
                .summary(item.summary())
                .content(item.content())
                .sourceCode(sourceCode)
                .sourceUrl(item.url())
                .languageCode(languageCode)
                .publishTime(item.publishTime() == null ? now : item.publishTime())
                .collectedAt(now)
                .createdAt(now)
                .build()))
            .count();
        return "已采集并保存 " + saved + " 条投资资讯";
    }
}
