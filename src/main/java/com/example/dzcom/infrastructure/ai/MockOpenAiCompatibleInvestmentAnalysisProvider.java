package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容协议投资分析 Provider。
 *
 * <p>当前用于验证数据库模型配置、Provider 选择和 API Key 注入链路。
 * 当 {@code mockEnabled=true} 时复用本地规则生成结构化报告，不发起外部网络请求；
 * 当 {@code mockEnabled=false} 时调用 OpenAI Chat Completions 兼容接口，要求模型返回
 * 与投资分析报告字段一致的 JSON。</p>
 */
@Component
@RequiredArgsConstructor
public class MockOpenAiCompatibleInvestmentAnalysisProvider
    implements InvestmentAnalysisProvider {

    private static final String PROVIDER_CODE = "OPENAI_COMPATIBLE";

    private final LocalRuleInvestmentAnalysisProvider localRuleProvider;
    private final ObjectMapper objectMapper;

    /**
     * 判断是否支持 OpenAI 兼容协议模型。
     *
     * @param providerCode 模型表中配置的 Provider 编码
     * @return 编码为 OPENAI_COMPATIBLE 时返回 true
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public boolean supports(String providerCode) {
        return PROVIDER_CODE.equals(providerCode);
    }

    /**
     * 使用已注入密钥的 OpenAI 兼容模拟配置生成报告。
     *
     * @param requestId 单次分析请求追踪标识
     * @param command 市场范围、主题和模拟资金等业务参数
     * @param modelConfig 数据库配置与外部 API Key 组成的运行配置
     * @return 标记为 OPENAI_COMPATIBLE 模型生成的结构化报告
     * @throws BusinessException 当模型未开启 mock 模式时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public InvestmentAnalysisReport analyze(
        String requestId,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        InvestmentAnalysisReport localReport = localRuleProvider.analyze(
            requestId,
            command,
            modelConfig
        );
        if (!modelConfig.mockEnabled()) {
            return callRemoteModel(localReport, command, modelConfig);
        }
        return InvestmentAnalysisReport.builder()
            .bizId(localReport.bizId())
            .requestId(localReport.requestId())
            .providerCode(PROVIDER_CODE)
            .modelCode(modelConfig.modelCode())
            .marketScope(localReport.marketScope())
            .themeCode(localReport.themeCode())
            .themeName(localReport.themeName())
            .status(localReport.status())
            .confidenceLevel(localReport.confidenceLevel())
            .dataQualityScore(localReport.dataQualityScore())
            .dataQualityGate(localReport.dataQualityGate())
            .investmentSummary(localReport.investmentSummary())
            .trend(localReport.trend())
            .investmentPlan(localReport.investmentPlan())
            .simulatedReturn(localReport.simulatedReturn())
            .chartPayload(localReport.chartPayload())
            .promptSnapshot(localReport.promptSnapshot())
            .failureReason(localReport.failureReason())
            .generatedAt(localReport.generatedAt())
            .createdAt(localReport.createdAt())
            .build();
    }

    /**
     * 调用 OpenAI 兼容远端模型并转换为投资分析报告。
     *
     * @param localReport 本地规则生成的可信输入和降级基线
     * @param command 投资分析命令
     * @param modelConfig 模型运行时配置
     * @return 远端模型结构化输出转换后的报告
     * @author dz
     * @date 2026-06-24
     */
    private InvestmentAnalysisReport callRemoteModel(
        InvestmentAnalysisReport localReport,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resolveChatCompletionsUrl(modelConfig.baseUrl())))
                .timeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .header("Authorization", "Bearer " + modelConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestPayload(localReport, command, modelConfig))))
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用失败: HTTP " + response.statusCode());
            }
            return mergeRemoteOutput(localReport, modelConfig, extractContent(response.body()));
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用被中断");
        } catch (Exception exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用失败: " + exception.getMessage());
        }
    }

    /**
     * 构建 OpenAI Chat Completions 请求体。
     *
     * @param localReport 本地规则报告
     * @param command 投资分析命令
     * @param modelConfig 模型配置
     * @return 请求 JSON 结构
     * @author dz
     * @date 2026-06-24
     */
    private Map<String, Object> requestPayload(
        InvestmentAnalysisReport localReport,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", modelConfig.remoteModel());
        payload.put("temperature", modelConfig.temperature());
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
            Map.of(
                "role", "system",
                "content", "你是投资辅助报告解析器，只能基于输入数据生成JSON。低质量数据必须输出数据缺口和风险提示，禁止给出确定性收益承诺。"
            ),
            Map.of(
                "role", "user",
                "content", prompt(localReport, command)
            )
        ));
        return payload;
    }

    /**
     * 构建发送给大模型的脱敏 Prompt。
     *
     * @param localReport 本地规则报告
     * @param command 投资分析命令
     * @return Prompt 文本
     * @author dz
     * @date 2026-06-24
     */
    private String prompt(InvestmentAnalysisReport localReport, GenerateInvestmentAnalysisCommand command) {
        return """
            请基于以下本地规则报告生成结构化投资分析 JSON。
            必须输出字段：investmentSummary、trend、investmentPlan、simulatedReturn、chartPayload、promptSnapshot。
            每个字段必须是 JSON 对象；不得输出 Markdown；不得编造缺失数据。
            marketScope=%s
            themeCode=%s
            lookbackDays=%s
            localDataQualityGate=%s
            localInvestmentSummary=%s
            localTrend=%s
            localInvestmentPlan=%s
            localSimulatedReturn=%s
            localChartPayload=%s
            """.formatted(
            localReport.marketScope(),
            command.themeCode(),
            command.lookbackDays(),
            localReport.dataQualityGate(),
            localReport.investmentSummary(),
            localReport.trend(),
            localReport.investmentPlan(),
            localReport.simulatedReturn(),
            localReport.chartPayload()
        );
    }

    /**
     * 合并远端模型输出和本地质量门禁。
     *
     * @param localReport 本地规则报告
     * @param modelConfig 模型配置
     * @param content 模型返回 JSON 文本
     * @return 可落库报告
     * @author dz
     * @date 2026-06-24
     */
    private InvestmentAnalysisReport mergeRemoteOutput(
        InvestmentAnalysisReport localReport,
        AiModelRuntimeConfig modelConfig,
        String content
    ) {
        JsonNode output = readJson(content);
        return InvestmentAnalysisReport.builder()
            .bizId(localReport.bizId())
            .requestId(localReport.requestId())
            .providerCode(PROVIDER_CODE)
            .modelCode(modelConfig.modelCode())
            .marketScope(localReport.marketScope())
            .themeCode(localReport.themeCode())
            .themeName(localReport.themeName())
            .status("SUCCEEDED")
            .confidenceLevel(localReport.confidenceLevel())
            .dataQualityScore(localReport.dataQualityScore())
            .dataQualityGate(localReport.dataQualityGate())
            .investmentSummary(requiredObject(output, "investmentSummary"))
            .trend(requiredObject(output, "trend"))
            .investmentPlan(requiredObject(output, "investmentPlan"))
            .simulatedReturn(requiredObject(output, "simulatedReturn"))
            .chartPayload(requiredObject(output, "chartPayload"))
            .promptSnapshot(requiredObject(output, "promptSnapshot"))
            .failureReason(null)
            .generatedAt(localReport.generatedAt())
            .createdAt(localReport.createdAt())
            .build();
    }

    private String resolveChatCompletionsUrl(String baseUrl) {
        String normalized = baseUrl == null || baseUrl.isBlank()
            ? "https://api.openai.com/v1"
            : baseUrl.replaceAll("/+$", "");
        return normalized.endsWith("/chat/completions")
            ? normalized
            : normalized + "/chat/completions";
    }

    private String extractContent(String responseBody) {
        JsonNode root = readJson(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型响应缺少 message.content");
        }
        return content.asText();
    }

    private String requiredObject(JsonNode output, String fieldName) {
        JsonNode value = output.get(fieldName);
        if (value == null || !value.isObject()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型输出缺少对象字段: " + fieldName);
        }
        return writeJson(value);
    }

    private JsonNode readJson(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型输出JSON格式不合法");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型请求JSON序列化失败");
        }
    }
}
