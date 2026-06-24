package com.example.dzcom.application.service.task;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.AiModelRuntimeConfigResolver;
import com.example.dzcom.application.service.ai.InvestmentAnalysisApplicationService;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.repository.ai.AiModelSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 自动投资报告生成任务测试。 */
class AutoInvestmentReportGenerationTaskHandlerTest {

    /** 自动报告任务应按配置主题批量调用投资分析服务。 */
    @Test
    void shouldGenerateReportsForConfiguredThemes() {
        CapturingAnalysisService analysis = new CapturingAnalysisService();
        AutoInvestmentReportGenerationTaskHandler handler =
            new AutoInvestmentReportGenerationTaskHandler(analysis);

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("auto-openai-investment-report-generation")
            .taskType("AUTO_INVESTMENT_REPORT_GENERATION")
            .parameters(Map.of(
                "providerCode", "OPENAI_COMPATIBLE",
                "modelCode", "openai-compatible-analysis",
                "marketScope", "CN_MAINLAND",
                "lookbackDays", "30",
                "initialCapital", "100000",
                "themes", "AI人工智能=159819;半导体=512480"
            ))
            .build());

        assertTrue(result.contains("2 份主题自动投资分析报告"));
        assertEquals(2, analysis.commands.size());
        assertEquals("AI人工智能", analysis.commands.get(0).themeCode());
        assertEquals("半导体", analysis.commands.get(1).themeCode());
        assertEquals(new BigDecimal("100000"), analysis.commands.get(0).initialCapital());
    }

    /** 捕获分析命令的测试服务。 */
    private static final class CapturingAnalysisService extends InvestmentAnalysisApplicationService {
        private final List<GenerateInvestmentAnalysisCommand> commands = new ArrayList<>();

        private CapturingAnalysisService() {
            super(
                List.of(new FixedProvider()),
                new FixedModelStore(),
                model -> AiModelRuntimeConfig.builder()
                    .modelCode(model.modelCode())
                    .modelVersion(model.modelVersion())
                    .providerCode(model.provider())
                    .mockEnabled(true)
                    .build(),
                new CapturingReportStore(),
                new FixedIdGenerator()
            );
        }

        @Override
        public InvestmentAnalysisReport generate(GenerateInvestmentAnalysisCommand command) {
            commands.add(command);
            return super.generate(command);
        }
    }

    /** 固定投资分析 Provider。 */
    private static final class FixedProvider implements InvestmentAnalysisProvider {
        @Override
        public boolean supports(String providerCode) {
            return "OPENAI_COMPATIBLE".equals(providerCode);
        }

        @Override
        public InvestmentAnalysisReport analyze(
            String requestId,
            GenerateInvestmentAnalysisCommand command,
            AiModelRuntimeConfig modelConfig
        ) {
            return InvestmentAnalysisReport.builder()
                .bizId("report-" + command.themeCode())
                .requestId(requestId)
                .providerCode("OPENAI_COMPATIBLE")
                .modelCode(modelConfig.modelCode())
                .marketScope(command.marketScope())
                .themeCode(command.themeCode())
                .status("SUCCEEDED")
                .generatedAt(LocalDateTime.of(2026, 6, 24, 10, 0))
                .createdAt(LocalDateTime.of(2026, 6, 24, 10, 0))
                .build();
        }
    }

    /** 固定模型仓储。 */
    private static final class FixedModelStore implements AiModelStore {
        @Override
        public Optional<AiModel> findByBizId(String bizId) {
            return Optional.empty();
        }

        @Override
        public Optional<AiModel> findByCodeAndVersion(String modelCode, String modelVersion) {
            return Optional.empty();
        }

        @Override
        public Optional<AiModel> findActiveByCode(String modelCode) {
            return Optional.of(AiModel.builder()
                .modelCode(modelCode)
                .modelVersion("default-v1")
                .provider("OPENAI_COMPATIBLE")
                .status("ACTIVE")
                .build());
        }

        @Override
        public AiModel save(AiModel model) {
            return model;
        }

        @Override
        public PageResult<AiModel> search(AiModelSearchCriteria criteria) {
            return PageResult.<AiModel>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }
    }

    /** 捕获报告保存的仓储。 */
    private static final class CapturingReportStore implements InvestmentAnalysisReportStore {
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
    }

    /** 固定业务 ID 生成器。 */
    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public String newBizId() {
            return "request-1";
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }
}
