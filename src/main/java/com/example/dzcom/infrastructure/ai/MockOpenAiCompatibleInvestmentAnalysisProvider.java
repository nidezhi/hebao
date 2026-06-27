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
import lombok.extern.slf4j.Slf4j;
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
 * <p>当前闭环要求真实调用远程模型。Provider 会先用本地规则生成可信输入基线，
 * 再强制调用 OpenAI Chat Completions 兼容接口；如果配置仍处于 mock 模式或远程
 * 调用失败，会记录错误日志并阻断任务。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
     * 使用 OpenAI 兼容远程模型生成报告。
     *
     * @param requestId 单次分析请求追踪标识
     * @param command 市场范围、主题和模拟资金等业务参数
     * @param modelConfig 数据库配置与外部 API Key 组成的运行配置
     * @return 标记为 OPENAI_COMPATIBLE 模型生成的结构化报告
     * @throws BusinessException 当模型仍处于 mock 模式或远程调用失败时抛出
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
        log.info(
            "投资分析模型准备远端调用: requestId={}, modelCode={}, modelVersion={}, providerCode={}, remoteModel={}, marketScope={}, themeCode={}, lookbackDays={}, localQualityScore={}, localGate={}",
            requestId,
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.remoteModel(),
            command.marketScope(),
            command.themeCode(),
            command.lookbackDays(),
            localReport.dataQualityScore(),
            localReport.dataQualityGate()
        );
        return callRemoteModel(localReport, command, modelConfig);
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
        validateRemoteCallable(localReport.requestId(), modelConfig);
        long startedAt = System.nanoTime();
        String endpoint = resolveChatCompletionsUrl(modelConfig.baseUrl());
        String userPrompt = prompt(localReport, command);
        log.info(
            "投资分析模型远端调用开始: requestId={}, modelCode={}, modelVersion={}, providerCode={}, remoteModel={}, endpoint={}, secretRef={}, apiKeyConfigured={}, timeoutSeconds={}, temperature={}, userPromptLength={}",
            localReport.requestId(),
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.remoteModel(),
            endpoint,
            modelConfig.secretRef(),
            modelConfig.apiKey() != null && !modelConfig.apiKey().isBlank(),
            modelConfig.timeoutSeconds(),
            modelConfig.temperature(),
            userPrompt.length()
        );
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .header("Authorization", "Bearer " + modelConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestPayload(userPrompt, modelConfig))))
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            log.info(
                "投资分析模型远端响应: requestId={}, modelCode={}, modelVersion={}, providerCode={}, httpStatus={}, durationMs={}, responseLength={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                response.statusCode(),
                elapsedMs(startedAt),
                textLength(response.body())
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用失败: HTTP " + response.statusCode());
            }
            String content = extractContent(response.body());
            InvestmentAnalysisReport report = mergeRemoteOutput(localReport, modelConfig, content);
            log.info(
                "投资分析模型远端调用完成: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, contentLength={}, qualityScore={}, confidenceLevel={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                textLength(content),
                report.dataQualityScore(),
                report.confidenceLevel()
            );
            return report;
        } catch (BusinessException exception) {
            log.error(
                "投资分析模型远端业务失败: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, reason={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                exception.getMessage()
            );
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error(
                "投资分析模型远端调用被中断: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt)
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用被中断");
        } catch (Exception exception) {
            log.error(
                "投资分析模型远端调用异常: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, exceptionType={}, reason={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                exception.getClass().getSimpleName(),
                exception.getMessage()
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用失败: " + exception.getMessage());
        }
    }

    /**
     * 校验投资分析报告必须真实调用远程模型。
     *
     * @param requestId 单次分析请求追踪标识
     * @param modelConfig 模型运行时配置
     * @throws BusinessException 当配置不可用于远程调用时抛出
     * @author dz
     * @date 2026-06-27
     */
    private void validateRemoteCallable(String requestId, AiModelRuntimeConfig modelConfig) {
        if (modelConfig.mockEnabled()) {
            failRemoteCallable(requestId, modelConfig, "模型配置mockEnabled=true，禁止使用本地规则替代远程模型");
        }
        if (isBlank(modelConfig.baseUrl())) {
            failRemoteCallable(requestId, modelConfig, "模型baseUrl为空，不能调用远程模型");
        }
        if (isBlank(modelConfig.remoteModel())) {
            failRemoteCallable(requestId, modelConfig, "远端模型名称为空，不能调用远程模型");
        }
        if (isBlank(modelConfig.apiKey())) {
            failRemoteCallable(requestId, modelConfig, "模型API Key为空，不能调用远程模型");
        }
    }

    /** 输出投资分析远程调用前置配置错误并抛出业务异常。 */
    private void failRemoteCallable(String requestId, AiModelRuntimeConfig modelConfig, String reason) {
        log.error(
            "投资分析模型远程调用前置校验失败: requestId={}, modelCode={}, modelVersion={}, providerCode={}, secretRef={}, reason={}",
            requestId,
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.secretRef(),
            reason
        );
        throw new BusinessException(HttpStatus.BAD_REQUEST, "投资分析模型远程调用失败: " + reason);
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
        String userPrompt,
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
                "content", userPrompt
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

    /** 计算文本长度，避免日志输出完整模型响应。 */
    private int textLength(String value) {
        return value == null ? 0 : value.length();
    }

    /** 判断文本是否为空。 */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 计算模型远端调用耗时毫秒。 */
    private long elapsedMs(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
