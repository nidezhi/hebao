package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.domain.model.ai.AiPromptEvaluation;
import com.example.dzcom.domain.model.ai.AiPromptOutputSchema;
import com.example.dzcom.domain.model.ai.AiPromptTemplate;
import com.example.dzcom.domain.model.ai.AiPromptVariable;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationStore;
import com.example.dzcom.domain.repository.ai.AiPromptSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiPromptStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 自动 Prompt 治理任务测试。 */
class AutoPromptGovernanceTaskHandlerTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 24, 10, 30);

    /** 无真实报告时只能初始化 Prompt 基线，不能写伪评估。 */
    @Test
    void shouldInitializePromptButNotCreateEvaluationWhenReportsEmpty() {
        MemoryPromptStore prompts = new MemoryPromptStore();
        MemoryPromptEvaluationStore evaluations = new MemoryPromptEvaluationStore();
        AutoPromptGovernanceTaskHandler handler = new AutoPromptGovernanceTaskHandler(
            prompts,
            evaluations,
            new EmptyReportStore(),
            new SequenceIdGenerator(),
            () -> NOW
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("auto-prompt-governance")
            .taskType("AUTO_PROMPT_GOVERNANCE")
            .parameters(Map.of(
                "promptCode", "investment-plan-from-report",
                "promptVersion", "auto-v1",
                "scenario", "INVESTMENT_PLAN"
            ))
            .build());

        var summary = Jsons.readObjectOrEmpty(result);
        assertEquals("INITIALIZED", Jsons.text(summary, "status"));
        assertTrue(Jsons.text(summary, "summary").contains("暂无真实报告"));
        assertEquals(1, prompts.templates.size());
        assertEquals(4, prompts.variables.size());
        assertEquals(1, prompts.schemas.size());
        assertEquals(0, evaluations.items.size());
    }

    /** 有真实报告时应写入 Prompt 评估记录。 */
    @Test
    void shouldCreateEvaluationFromRealReport() {
        MemoryPromptStore prompts = new MemoryPromptStore();
        MemoryPromptEvaluationStore evaluations = new MemoryPromptEvaluationStore();
        AutoPromptGovernanceTaskHandler handler = new AutoPromptGovernanceTaskHandler(
            prompts,
            evaluations,
            new FixedReportStore(),
            new SequenceIdGenerator(),
            () -> NOW
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("auto-prompt-governance")
            .taskType("AUTO_PROMPT_GOVERNANCE")
            .parameters(Map.of())
            .build());

        var summary = Jsons.readObjectOrEmpty(result);
        assertEquals("SUCCEEDED", Jsons.text(summary, "status"));
        assertEquals("report-1", Jsons.text(summary, "reportBizId"));
        assertEquals("test-id-7", Jsons.text(summary, "evaluationBizId"));
        assertTrue(Jsons.text(summary, "renderedPromptPreview").contains("report-1"));
        assertEquals(1, evaluations.items.size());
        assertEquals("investment-plan-from-report", evaluations.items.get(0).promptCode());
        assertEquals("REPORT_PROMPT_GOVERNANCE", evaluations.items.get(0).scenario());
        assertTrue(evaluations.items.get(0).scoreDetail().contains("renderedPromptPreview"));
    }

    /** 内存 Prompt 仓储。 */
    private static final class MemoryPromptStore implements AiPromptStore {
        private final List<AiPromptTemplate> templates = new ArrayList<>();
        private final List<AiPromptVariable> variables = new ArrayList<>();
        private final List<AiPromptOutputSchema> schemas = new ArrayList<>();

        @Override
        public AiPromptTemplate saveTemplate(AiPromptTemplate template) {
            templates.removeIf(item -> item.promptCode().equals(template.promptCode())
                && item.promptVersion().equals(template.promptVersion()));
            templates.add(template);
            return template;
        }

        @Override
        public void replaceVariables(String promptBizId, List<AiPromptVariable> newVariables) {
            variables.removeIf(item -> item.promptBizId().equals(promptBizId));
            variables.addAll(newVariables);
        }

        @Override
        public void replaceOutputSchemas(String promptBizId, List<AiPromptOutputSchema> newSchemas) {
            schemas.removeIf(item -> item.promptBizId().equals(promptBizId));
            schemas.addAll(newSchemas);
        }

        @Override
        public Optional<AiPromptTemplate> findTemplateByBizId(String bizId) {
            return templates.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public Optional<AiPromptTemplate> findTemplateByCodeAndVersion(String promptCode, String promptVersion) {
            return templates.stream()
                .filter(item -> item.promptCode().equals(promptCode) && item.promptVersion().equals(promptVersion))
                .findFirst();
        }

        @Override
        public List<AiPromptVariable> findVariables(String promptBizId) {
            return variables.stream().filter(item -> item.promptBizId().equals(promptBizId)).toList();
        }

        @Override
        public List<AiPromptOutputSchema> findOutputSchemas(String promptBizId) {
            return schemas.stream().filter(item -> item.promptBizId().equals(promptBizId)).toList();
        }

        @Override
        public PageResult<AiPromptTemplate> search(AiPromptSearchCriteria criteria) {
            return PageResult.<AiPromptTemplate>builder()
                .items(templates)
                .total(templates.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(templates.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 内存 Prompt 评估仓储。 */
    private static final class MemoryPromptEvaluationStore implements AiPromptEvaluationStore {
        private final List<AiPromptEvaluation> items = new ArrayList<>();

        @Override
        public AiPromptEvaluation save(AiPromptEvaluation evaluation) {
            items.add(evaluation);
            return evaluation;
        }

        @Override
        public Optional<AiPromptEvaluation> findByBizId(String bizId) {
            return items.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public PageResult<AiPromptEvaluation> search(AiPromptEvaluationSearchCriteria criteria) {
            return PageResult.<AiPromptEvaluation>builder()
                .items(items)
                .total(items.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(items.isEmpty() ? 0 : 1)
                .build();
        }
    }

    /** 空报告仓储。 */
    private static class EmptyReportStore implements InvestmentAnalysisReportStore {
        @Override
        public InvestmentAnalysisReport save(InvestmentAnalysisReport report) {
            return report;
        }

        @Override
        public Optional<InvestmentAnalysisReport> findByBizId(String bizId) {
            return Optional.empty();
        }

        @Override
        public PageResult<InvestmentAnalysisReport> search(InvestmentAnalysisReportSearchCriteria criteria) {
            return PageResult.<InvestmentAnalysisReport>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }

        @Override
        public PageResult<InvestmentAnalysisReport> latest(int size) {
            return PageResult.<InvestmentAnalysisReport>builder()
                .items(List.of())
                .total(0)
                .page(1)
                .size(size)
                .totalPages(0)
                .build();
        }
    }

    /** 固定一份真实报告的仓储。 */
    private static final class FixedReportStore extends EmptyReportStore {
        @Override
        public PageResult<InvestmentAnalysisReport> latest(int size) {
            return PageResult.<InvestmentAnalysisReport>builder()
                .items(List.of(InvestmentAnalysisReport.builder()
                    .bizId("report-1")
                    .status("SUCCEEDED")
                    .confidenceLevel("HIGH_CONFIDENCE")
                    .dataQualityScore(new BigDecimal("0.90"))
                    .dataQualityGate("{\"passed\":true}")
                    .investmentSummary("{\"sampleCount\":8}")
                    .trend("{\"direction\":\"UP\"}")
                    .investmentPlan("{\"planType\":\"REFERENCE_ALLOCATION\",\"referenceAllocationAmount\":1000}")
                    .simulatedReturn("{\"estimatedProfit\":120}")
                    .themeCode("AI")
                    .generatedAt(NOW)
                    .createdAt(NOW)
                    .build()))
                .total(1)
                .page(1)
                .size(size)
                .totalPages(1)
                .build();
        }
    }

    /** 顺序业务 ID 生成器。 */
    private static final class SequenceIdGenerator implements IdGenerator {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public String newBizId() {
            return "test-id-" + sequence.incrementAndGet();
        }

        @Override
        public String newUserNo() {
            return "U-" + sequence.incrementAndGet();
        }
    }
}
