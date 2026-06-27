package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.AiJsonCompletionClient;
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

/** OpenAI Chat Completions 兼容 JSON 调用客户端。 */
@Component
@RequiredArgsConstructor
@Slf4j
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
     * <p>当前闭环要求真实调用远程模型，因此不再支持 mock 短路或空配置兜底。
     * 如果远程模型不可调用，方法会记录错误日志并抛出业务异常。</p>
     *
     * @param operationCode 操作编码
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param modelConfig 模型运行时配置
     * @return JSON 对象文本
     * @throws BusinessException 当配置仍处于 mock 模式或远程调用失败时抛出
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
        validateRemoteCallable(operationCode, modelConfig);
        long startedAt = System.nanoTime();
        String endpoint = resolveChatCompletionsUrl(modelConfig.baseUrl());
        log.info(
            "AI JSON模型调用开始: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, remoteModel={}, endpoint={}, httpMethod=POST, secretRef={}, apiKeyConfigured={}, timeoutSeconds={}, maxTokens={}, temperature={}, systemPromptLength={}, userPromptLength={}",
            operationCode,
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.remoteModel(),
            endpoint,
            modelConfig.secretRef(),
            modelConfig.apiKey() != null && !modelConfig.apiKey().isBlank(),
            modelConfig.timeoutSeconds(),
            modelConfig.maxTokens(),
            modelConfig.temperature(),
            textLength(systemPrompt),
            textLength(userPrompt)
        );
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .header("Authorization", "Bearer " + modelConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestPayload(systemPrompt, userPrompt, modelConfig))))
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            long durationMs = elapsedMs(startedAt);
            log.info(
                "AI JSON模型调用响应: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, httpStatus={}, durationMs={}, responseLength={}",
                operationCode,
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                response.statusCode(),
                durationMs,
                textLength(response.body())
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error(
                    "AI JSON模型调用远端失败: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, endpoint={}, httpMethod=POST, httpStatus={}, durationMs={}, responseBody={}",
                    operationCode,
                    modelConfig.modelCode(),
                    modelConfig.modelVersion(),
                    modelConfig.providerCode(),
                    endpoint,
                    response.statusCode(),
                    durationMs,
                    limit(response.body(), 2000)
                );
                throw new BusinessException(HttpStatus.BAD_GATEWAY,
                    operationCode + "模型调用失败: HTTP " + response.statusCode()
                        + ", body=" + limit(response.body(), 500));
            }
            String content = normalizeJsonObjectContent(extractContent(response.body(), operationCode), operationCode);
            log.info(
                "AI JSON模型调用完成: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, contentLength={}",
                operationCode,
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                textLength(content)
            );
            return content;
        } catch (BusinessException exception) {
            log.error(
                "AI JSON模型调用业务失败: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, reason={}",
                operationCode,
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
                "AI JSON模型调用被中断: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}",
                operationCode,
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt)
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY, operationCode + "模型调用被中断");
        } catch (Exception exception) {
            log.error(
                "AI JSON模型调用异常: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, exceptionType={}, reason={}",
                operationCode,
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                exception.getClass().getSimpleName(),
                exception.getMessage()
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                operationCode + "模型调用失败: " + exception.getMessage());
        }
    }

    /**
     * 校验本次 JSON 补全必须真实调用远程模型。
     *
     * @param operationCode 操作编码
     * @param modelConfig 模型运行时配置
     * @throws BusinessException 当配置不可用于远程调用时抛出
     * @author dz
     * @date 2026-06-27
     */
    private void validateRemoteCallable(String operationCode, AiModelRuntimeConfig modelConfig) {
        if (modelConfig.mockEnabled()) {
            failRemoteCallable(operationCode, modelConfig, "模型配置mockEnabled=true，禁止跳过远程大模型调用");
        }
        if (isBlank(modelConfig.baseUrl())) {
            failRemoteCallable(operationCode, modelConfig, "模型baseUrl为空，不能调用远程大模型");
        }
        if (isBlank(modelConfig.remoteModel())) {
            failRemoteCallable(operationCode, modelConfig, "远端模型名称为空，不能调用远程大模型");
        }
        if (isBlank(modelConfig.apiKey())) {
            failRemoteCallable(operationCode, modelConfig, "模型API Key为空，不能调用远程大模型");
        }
    }

    /** 输出远程调用前置配置错误并抛出业务异常。 */
    private void failRemoteCallable(String operationCode, AiModelRuntimeConfig modelConfig, String reason) {
        log.error(
            "AI JSON模型远程调用前置校验失败: operationCode={}, modelCode={}, modelVersion={}, providerCode={}, secretRef={}, reason={}",
            operationCode,
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.secretRef(),
            reason
        );
        throw new BusinessException(HttpStatus.BAD_REQUEST, operationCode + reason);
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
        if (modelConfig.maxTokens() > 0) {
            payload.put("max_tokens", modelConfig.maxTokens());
        }
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

    /** 规范化模型输出，允许从代码块或解释文本中提取第一个 JSON 对象。 */
    private String normalizeJsonObjectContent(String value, String operationCode) {
        String stripped = stripMarkdownFence(value);
        if (isJsonObject(stripped)) {
            return stripped;
        }
        String extracted = firstJsonObject(stripped);
        if (extracted != null && isJsonObject(extracted)) {
            log.warn(
                "AI JSON模型输出包含非JSON包裹文本，已提取首个JSON对象: operationCode={}, originalLength={}, extractedLength={}",
                operationCode,
                textLength(value),
                textLength(extracted)
            );
            return extracted;
        }
        log.error(
            "AI JSON模型输出JSON格式不合法: operationCode={}, contentPreview={}",
            operationCode,
            limit(value, 1200)
        );
        throw new BusinessException(HttpStatus.BAD_GATEWAY, operationCode + "模型输出JSON格式不合法");
    }

    /** 判断文本是否为 JSON 对象。 */
    private boolean isJsonObject(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            return objectMapper.readTree(value).isObject();
        } catch (JsonProcessingException exception) {
            return false;
        }
    }

    /** 去掉常见 Markdown 代码围栏。 */
    private String stripMarkdownFence(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineEnd < 0 || lastFence <= firstLineEnd) {
            return trimmed;
        }
        return trimmed.substring(firstLineEnd + 1, lastFence).trim();
    }

    /** 从文本中提取第一个括号平衡的 JSON 对象。 */
    private String firstJsonObject(String value) {
        if (value == null) {
            return null;
        }
        int start = value.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return value.substring(start, index + 1).trim();
                }
            }
        }
        return null;
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

    /** 计算文本长度，日志只记录长度，避免泄露完整 Prompt 和模型响应。 */
    private int textLength(String value) {
        return value == null ? 0 : value.length();
    }

    /** 截断远端错误响应，保留排查线索并避免日志过长。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 判断文本是否为空。 */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 计算模型调用耗时毫秒。 */
    private long elapsedMs(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
