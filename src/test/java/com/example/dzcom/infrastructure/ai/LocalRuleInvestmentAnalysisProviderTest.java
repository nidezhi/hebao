package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** 本地规则投资分析 Provider 测试。 */
class LocalRuleInvestmentAnalysisProviderTest {

    /**
     * 空样本必须降级为数据缺口报告，不能输出配置建议。
     *
     * @throws Exception JSON 解析失败时抛出
     * @author dz
     * @date 2026-06-22
     */
    @Test
    void shouldDowngradeToDataGapReportWhenSamplesAreEmpty() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 10, 0);
        LocalRuleInvestmentAnalysisProvider provider = new LocalRuleInvestmentAnalysisProvider(
            new EmptySnapshotStore(),
            new EmptyNewsArticleStore(),
            new FixedIdGenerator(),
            () -> now,
            objectMapper
        );

        InvestmentAnalysisReport report = provider.analyze(
            "request-1",
            GenerateInvestmentAnalysisCommand.builder()
                .marketScope("CN_MAINLAND")
                .themeCode("AI人工智能")
                .lookbackDays(30)
                .initialCapital(BigDecimal.valueOf(100000))
                .build(),
            AiModelRuntimeConfig.builder()
                .modelCode("local-rule-analysis")
                .providerCode("LOCAL_RULE")
                .mockEnabled(false)
                .build()
        );

        JsonNode dataQualityGate = objectMapper.readTree(report.dataQualityGate());
        JsonNode investmentPlan = objectMapper.readTree(report.investmentPlan());
        assertEquals("UNUSABLE", report.confidenceLevel());
        assertFalse(dataQualityGate.get("passed").asBoolean());
        assertEquals("DATA_GAP_REPORT", investmentPlan.get("planType").asText());
        assertEquals(0, investmentPlan.get("referenceAllocationRate").decimalValue()
            .compareTo(BigDecimal.ZERO));
    }

    /** 空快照仓储假实现。 */
    private static final class EmptySnapshotStore implements InvestmentThemeSnapshotStore {
        @Override
        public InvestmentThemeSnapshot save(InvestmentThemeSnapshot snapshot) {
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

    /** 空资讯仓储假实现。 */
    private static final class EmptyNewsArticleStore implements NewsArticleStore {
        @Override
        public NewsArticle save(NewsArticle article) {
            return article;
        }

        @Override
        public long countByKeywords(List<String> keywords, LocalDateTime from) {
            return 0;
        }

        @Override
        public List<NewsArticle> findRecentByKeywords(
            List<String> keywords,
            LocalDateTime from,
            int limit
        ) {
            return List.of();
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

    /** 固定业务 ID 生成器。 */
    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public String newBizId() {
            return "report-1";
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }
}
