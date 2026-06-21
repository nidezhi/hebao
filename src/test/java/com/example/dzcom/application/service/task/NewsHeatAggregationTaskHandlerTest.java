package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.NewsArticleRelation;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleRelationStore;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 资讯热度汇总任务测试。 */
class NewsHeatAggregationTaskHandlerTest {

    /**
     * 资讯热度任务应保存质量指标和新闻主题产品显式关联。
     *
     * @throws Exception JSON 解析失败时抛出
     * @author dz
     * @date 2026-06-21
     */
    @Test
    void shouldSaveQualityMetricsAndArticleRelations() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 6, 21, 10, 0);
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        CapturingSnapshotStore snapshots = new CapturingSnapshotStore();
        CapturingRelationStore relations = new CapturingRelationStore();
        NewsHeatAggregationTaskHandler handler = new NewsHeatAggregationTaskHandler(
            new FixedNewsArticleStore(now),
            relations,
            snapshots,
            new IncrementIdGenerator(),
            () -> now,
            objectMapper
        );

        handler.execute(InvestmentTaskEvent.builder()
            .taskCode("news-heat-aggregation")
            .taskType("NEWS_HEAT_AGGREGATION")
            .parameters(Map.of(
                "windowMinutes", "1440",
                "marketScope", "CN_MAINLAND",
                "themes", "AI人工智能=AI,人工智能,算力",
                "themeProducts", "AI人工智能=159819,588000"
            ))
            .build());

        JsonNode metrics = objectMapper.readTree(snapshots.saved.metrics());
        assertEquals("AI人工智能", snapshots.saved.themeName());
        assertTrue(snapshots.saved.heatScore().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(metrics.get("dataQualityScore").decimalValue().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(2, relations.saved.size());
        assertEquals("159819", relations.saved.get(0).productCode());
        assertTrue(relations.saved.get(0).matchedKeywords().contains("AI"));
    }

    /** 固定新闻数据的资讯仓储假实现。 */
    private record FixedNewsArticleStore(LocalDateTime now) implements NewsArticleStore {
        @Override
        public NewsArticle save(NewsArticle article) {
            return article;
        }

        @Override
        public long countByKeywords(List<String> keywords, LocalDateTime from) {
            return 1;
        }

        @Override
        public List<NewsArticle> findRecentByKeywords(
            List<String> keywords,
            LocalDateTime from,
            int limit
        ) {
            return List.of(NewsArticle.builder()
                .bizId("article-1")
                .externalId("external-1")
                .articleType("NEWS")
                .title("AI 算力产业链投资热度提升")
                .summary("人工智能应用带动算力需求")
                .content("AI、人工智能和算力方向持续受到关注")
                .sourceCode("CN_MAINLAND_NEWS")
                .sourceUrl("https://example.com/news/1")
                .languageCode("zh-CN")
                .publishTime(now.minusHours(2))
                .collectedAt(now)
                .createdAt(now)
                .build());
        }

        @Override
        public PageResult<NewsArticle> search(NewsArticleSearchCriteria criteria) {
            return PageResult.<NewsArticle>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }
    }

    /** 捕获主题快照保存结果。 */
    private static final class CapturingSnapshotStore implements InvestmentThemeSnapshotStore {
        private InvestmentThemeSnapshot saved;

        @Override
        public InvestmentThemeSnapshot save(InvestmentThemeSnapshot snapshot) {
            saved = snapshot;
            return snapshot;
        }

        @Override
        public PageResult<InvestmentThemeSnapshot> search(
            InvestmentThemeSnapshotSearchCriteria criteria
        ) {
            return PageResult.<InvestmentThemeSnapshot>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }
    }

    /** 捕获新闻关联保存结果。 */
    private static final class CapturingRelationStore implements NewsArticleRelationStore {
        private final List<NewsArticleRelation> saved = new ArrayList<>();

        @Override
        public void saveBatch(List<NewsArticleRelation> relations) {
            saved.addAll(relations);
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
