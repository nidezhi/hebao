package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.AiJsonCompletionClient;
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

/** OpenAI Chat Completions 兼容 JSON 调用客户端。 */
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleJsonCompletionClient implements AiJsonCompletionClient {
    private static final String PROVIDER_CODE = "OPENAI_COMPATIBLE";

    private final ObjectMapper objectMapper;

    /**
     * 判断是否支持 OpenAI 兼容 Provider。
     *
     * @param providerCode 模型 Provider 编码
     * @return Provider 为 OPENAI_COMPATIBLE 时返回 true
     * @author dz
     * @date 2026-06-27
     */
    @Override
    public boolean supports(String providerCode) {
        return PROVIDER_CODE.equals(providerCode);
    }

    /**
     * 调用 OpenAI 兼容接口并返回 message.content 中的 JSON 文本。
     *
     * <p>当模型配置 {@code mockEnabled=true} 时不发起外部网络请求，返回空字符串，
     * 由上层业务使用可审计的本地兜底结果完成链路验证。</p>
     *
     * @param operationCode 操作编码
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param modelConfig 模型运行时配置
     * @return JSON 对象文本，mock 模式返回空字符串
     * @author dz
     * @date 2026-06-27
     */
    @Override
    public String completeJson(
        String operationCode,
        String systemPrompt,
        String userPrompt,
        AiModelRuntimeConfig modelConfig
    ) {
        if (modelConfig.mockEnabled()) {
            return "";
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resolveChatCompletionsUrl(modelConfig.baseUrl())))
                .timeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .header("Authorization", "Bearer " + modelConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestPayload(systemPrompt, userPrompt, modelConfig))))
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY,
                    operationCode + "模型调用失败: HTTP " + response.statusCode());
            }
            String content = extractContent(response.body(), operationCode);
            validateJsonObject(content, operationCode);
            return content;
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, operationCode + "模型调用被中断");
        } catch (Exception exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                operationCode + "模型调用失败: " + exception.getMessage());
        }
    }

    /** 构造 Chat Completions 请求体。 */
    private Map<String, Object> requestPayload(
        String systemPrompt,
        String userPrompt,
        AiModelRuntimeConfig modelConfig
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", modelConfig.remoteModel());
        payload.put("temperature", modelConfig.temperature());
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ));
        return payload;
    }

    /** 解析 OpenAI 兼容接口地址。 */
    private String resolveChatCompletionsUrl(String baseUrl) {
        String normalized = baseUrl == null || baseUrl.isBlank()
            ? "https://api.openai.com/v1"
            : baseUrl.replaceAll("/+$", "");
        return normalized.endsWith("/chat/completions")
            ? normalized
            : normalized + "/chat/completions";
    }

    /** 提取模型响应正文。 */
    private String extractContent(String responseBody, String operationCode) {
        JsonNode root = readJson(responseBody, operationCode);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, operationCode + "模型响应缺少 message.content");
        }
        return content.asText();
    }

    /** 校验模型输出是 JSON 对象。 */
    private void validateJsonObject(String value, String operationCode) {
        if (!readJson(value, operationCode).isObject()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, operationCode + "模型输出必须是JSON对象");
        }
    }

    /** 读取 JSON。 */
    private JsonNode readJson(String value, String operationCode) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, operationCode + "模型输出JSON格式不合法");
        }
    }

    /** 序列化 JSON。 */
    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "模型请求JSON序列化失败");
        }
    }
}
