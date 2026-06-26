package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.AiModelRuntimeConfigResolver;
import com.example.dzcom.application.service.ai.AiSecretResolver;
import com.example.dzcom.domain.model.ai.AiModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 使用 Jackson 解析模型 JSON 配置并注入外部密钥。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JacksonAiModelRuntimeConfigResolver implements AiModelRuntimeConfigResolver {
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    private static final BigDecimal DEFAULT_TEMPERATURE = new BigDecimal("0.2");

    private final ObjectMapper objectMapper;
    private final AiSecretResolver secretResolver;

    /**
     * 将 ACTIVE 模型记录解析为 Provider 可直接使用的配置。
     *
     * @param model 已注册的 AI 模型版本
     * @return 包含普通参数和外部 API Key 的运行时配置
     * @throws BusinessException 当 JSON 非法或必要配置缺失时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public AiModelRuntimeConfig resolve(AiModel model) {
        JsonNode config = readConfig(model.modelConfig());
        String secretRef = text(config, "secretRef");
        boolean mockEnabled = bool(config, "mockEnabled", false);
        log.info(
            "AI模型运行配置解析开始: modelCode={}, modelVersion={}, providerCode={}, secretRef={}, mockEnabled={}, baseUrlConfigured={}, remoteModel={}",
            model.modelCode(),
            model.modelVersion(),
            model.provider(),
            secretRef,
            mockEnabled,
            !text(config, "baseUrl").isBlank(),
            text(config, "model")
        );
        String apiKey = resolveSecret(model, secretRef);

        AiModelRuntimeConfig runtimeConfig = AiModelRuntimeConfig.builder()
            .modelCode(model.modelCode())
            .modelVersion(model.modelVersion())
            .providerCode(model.provider())
            .baseUrl(text(config, "baseUrl"))
            .remoteModel(text(config, "model"))
            .secretRef(secretRef)
            .apiKey(apiKey)
            .timeoutSeconds(integer(config, "timeoutSeconds", DEFAULT_TIMEOUT_SECONDS))
            .temperature(decimal(config, "temperature", DEFAULT_TEMPERATURE))
            .mockEnabled(mockEnabled)
            .build();
        log.info(
            "AI模型运行配置解析完成: modelCode={}, modelVersion={}, providerCode={}, secretRef={}, apiKeyConfigured={}, mockEnabled={}, timeoutSeconds={}, temperature={}",
            runtimeConfig.modelCode(),
            runtimeConfig.modelVersion(),
            runtimeConfig.providerCode(),
            runtimeConfig.secretRef(),
            runtimeConfig.apiKey() != null && !runtimeConfig.apiKey().isBlank(),
            runtimeConfig.mockEnabled(),
            runtimeConfig.timeoutSeconds(),
            runtimeConfig.temperature()
        );
        return runtimeConfig;
    }

    /** 解析外部密钥并记录失败原因，日志不输出密钥明文。 */
    private String resolveSecret(AiModel model, String secretRef) {
        if (secretRef.isBlank()) {
            return "";
        }
        try {
            return secretResolver.resolve(secretRef);
        } catch (BusinessException exception) {
            log.warn(
                "AI模型密钥解析失败: modelCode={}, modelVersion={}, providerCode={}, secretRef={}, reason={}",
                model.modelCode(),
                model.modelVersion(),
                model.provider(),
                secretRef,
                exception.getMessage()
            );
            throw exception;
        }
    }

    /**
     * 读取模型配置 JSON；空配置按空对象处理。
     *
     * @param modelConfig 数据库存储的模型配置 JSON
     * @return 可安全读取字段的 JSON 节点
     * @throws BusinessException 当 JSON 格式非法时抛出
     * @author dz
     * @date 2026-06-18
     */
    private JsonNode readConfig(String modelConfig) {
        if (modelConfig == null || modelConfig.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(modelConfig);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI模型配置JSON格式不合法");
        }
    }

    /** 读取字符串配置，字段不存在时返回空字符串。 */
    private String text(JsonNode config, String fieldName) {
        JsonNode value = config.get(fieldName);
        return value == null || value.isNull() ? "" : value.asText("");
    }

    /** 读取整数配置，字段不存在时返回默认值。 */
    private int integer(JsonNode config, String fieldName, int defaultValue) {
        JsonNode value = config.get(fieldName);
        return value == null || !value.canConvertToInt() ? defaultValue : value.asInt();
    }

    /** 读取小数配置，字段不存在时返回默认值。 */
    private BigDecimal decimal(JsonNode config, String fieldName, BigDecimal defaultValue) {
        JsonNode value = config.get(fieldName);
        return value == null || !value.isNumber() ? defaultValue : value.decimalValue();
    }

    /** 读取布尔配置，字段不存在时返回默认值。 */
    private boolean bool(JsonNode config, String fieldName, boolean defaultValue) {
        JsonNode value = config.get(fieldName);
        return value == null || !value.isBoolean() ? defaultValue : value.asBoolean();
    }
}
