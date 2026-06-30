package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** OpenAI 兼容投资分析 Provider 测试。 */
class MockOpenAiCompatibleInvestmentAnalysisProviderTest {

    /**
     * 远端模型偶发包裹说明文本时，应提取首个 JSON 对象而不是让自动报告任务失败。
     *
     * @throws Exception 反射或 JSON 解析失败时抛出
     * @author dz
     * @date 2026-06-28
     */
    @Test
    void shouldMergeRemoteOutputWhenJsonIsWrappedByText() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        MockOpenAiCompatibleInvestmentAnalysisProvider provider =
            new MockOpenAiCompatibleInvestmentAnalysisProvider(null, objectMapper);

        InvestmentAnalysisReport report = mergeRemoteOutput(
            provider,
            localReport(),
            modelConfig(),
            """
                已根据输入生成结构化结果：
                {
                  "investmentSummary": {"summary": "全市场报告"},
                  "trend": {"direction": "NEUTRAL"},
                  "investmentPlan": {"planType": "REFERENCE_ALLOCATION"},
                  "simulatedReturn": {"returnRate": 0.01},
                  "chartPayload": {"series": []},
                  "promptSnapshot": {"source": "remote"}
                }
                请以系统解析为准。
                """,
            "{\"requestMessages\":[{\"role\":\"user\",\"contentPreview\":\"safe\",\"contentLength\":4}]}"
        );

        assertEquals("SUCCEEDED", report.status());
        assertEquals("OPENAI_COMPATIBLE", report.providerCode());
        assertEquals("{\"summary\":\"全市场报告\"}", report.investmentSummary());
        assertEquals("{\"direction\":\"NEUTRAL\"}", report.trend());
        assertEquals("{\"requestMessages\":[{\"role\":\"user\",\"contentPreview\":\"safe\",\"contentLength\":4}]}", report.chatSnapshot());
        assertFalse(report.chatSnapshot().contains("Authorization"));
        assertFalse(report.chatSnapshot().contains("test"));
    }

    private InvestmentAnalysisReport mergeRemoteOutput(
        MockOpenAiCompatibleInvestmentAnalysisProvider provider,
        InvestmentAnalysisReport localReport,
        AiModelRuntimeConfig modelConfig,
        String content,
        String chatSnapshot
    ) throws Exception {
        Method method = MockOpenAiCompatibleInvestmentAnalysisProvider.class
            .getDeclaredMethod(
                "mergeRemoteOutput",
                InvestmentAnalysisReport.class,
                AiModelRuntimeConfig.class,
                String.class,
                String.class
            );
        method.setAccessible(true);
        return (InvestmentAnalysisReport) method.invoke(provider, localReport, modelConfig, content, chatSnapshot);
    }

    private InvestmentAnalysisReport localReport() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 28, 23, 30);
        return InvestmentAnalysisReport.builder()
            .bizId("report-1")
            .requestId("request-1")
            .providerCode("LOCAL_RULE")
            .modelCode("local-rule-analysis")
            .marketScope("CN_MAINLAND")
            .themeCode(null)
            .themeName(null)
            .status("SUCCEEDED")
            .confidenceLevel("MEDIUM_CONFIDENCE")
            .dataQualityScore(new BigDecimal("0.6500"))
            .dataQualityGate("{\"passed\":true}")
            .investmentSummary("{\"summary\":\"local\"}")
            .trend("{\"direction\":\"NEUTRAL\"}")
            .investmentPlan("{\"planType\":\"REFERENCE_ALLOCATION\"}")
            .simulatedReturn("{\"returnRate\":0}")
            .chartPayload("{\"series\":[]}")
            .promptSnapshot("{\"source\":\"local\"}")
            .generatedAt(now)
            .createdAt(now)
            .build();
    }

    private AiModelRuntimeConfig modelConfig() {
        return AiModelRuntimeConfig.builder()
            .modelCode("openai-compatible-analysis")
            .modelVersion("default-v1")
            .providerCode("OPENAI_COMPATIBLE")
            .baseUrl("https://example.test/v1")
            .remoteModel("gpt-5.5")
            .secretRef("OPENAI_API_KEY")
            .apiKey("test")
            .timeoutSeconds(60)
            .temperature(new BigDecimal("0.2"))
            .mockEnabled(false)
            .build();
    }
}
