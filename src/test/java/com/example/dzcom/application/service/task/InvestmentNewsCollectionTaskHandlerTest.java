package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 投资资讯采集任务测试。 */
class InvestmentNewsCollectionTaskHandlerTest {

    /** 外部源无数据且禁用兜底时，不应写入低质量兜底资讯。 */
    @Test
    void shouldSkipFallbackArticlesWhenFallbackDisabled() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);
        CapturingNewsArticleStore articles = new CapturingNewsArticleStore();
        InvestmentNewsCollectionTaskHandler handler = new InvestmentNewsCollectionTaskHandler(
            (feedUrl, maxItems) -> List.of(),
            articles,
            new FixedIdGenerator(),
            () -> now
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("l1-regulatory-news-collection")
            .taskType("INVESTMENT_NEWS_COLLECTION")
            .parameters(Map.of(
                "sourceCode", "CSRC",
                "fallbackEnabled", "false",
                "fallbackArticles", "AI人工智能|兜底标题|兜底摘要"
            ))
            .build());

        assertTrue(result.contains("禁用兜底资讯写入"));
        assertEquals(0, articles.saved.size());
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

    /** 固定业务 ID 生成器。 */
    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public String newBizId() {
            return "article-1";
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }
}
