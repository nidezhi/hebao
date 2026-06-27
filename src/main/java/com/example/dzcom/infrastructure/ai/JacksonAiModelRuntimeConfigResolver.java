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
    private static final int DEFAULT_MAX_TOKENS = 0;
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
        String baseUrl = text(config, "baseUrl");
        String remoteModel = text(config, "model");
        log.info(
            "AI模型运行配置解析开始: modelCode={}, modelVersion={}, providerCode={}, secretRef={}, mockEnabled={}, baseUrlConfigured={}, remoteModel={}",
            model.modelCode(),
            model.modelVersion(),
            model.provider(),
            secretRef,
            mockEnabled,
            !baseUrl.isBlank(),
            remoteModel
        );
        validateRemoteConfig(model, secretRef, baseUrl, remoteModel, mockEnabled);
        String apiKey = resolveSecret(model, secretRef);
        validateApiKey(model, secretRef, apiKey);

        AiModelRuntimeConfig runtimeConfig = AiModelRuntimeConfig.builder()
            .modelCode(model.modelCode())
            .modelVersion(model.modelVersion())
            .providerCode(model.provider())
            .baseUrl(baseUrl)
            .remoteModel(remoteModel)
            .secretRef(secretRef)
            .apiKey(apiKey)
            .timeoutSeconds(integer(config, "timeoutSeconds", DEFAULT_TIMEOUT_SECONDS))
            .maxTokens(integer(config, "maxTokens",
                integer(config, "maxCompletionTokens", DEFAULT_MAX_TOKENS)))
            .temperature(decimal(config, "temperature", DEFAULT_TEMPERATURE))
            .mockEnabled(mockEnabled)
            .build();
        log.info(
            "AI模型运行配置解析完成: modelCode={}, modelVersion={}, providerCode={}, secretRef={}, apiKeyConfigured={}, mockEnabled={}, timeoutSeconds={}, maxTokens={}, temperature={}",
            runtimeConfig.modelCode(),
            runtimeConfig.modelVersion(),
            runtimeConfig.providerCode(),
            runtimeConfig.secretRef(),
            runtimeConfig.apiKey() != null && !runtimeConfig.apiKey().isBlank(),
            runtimeConfig.mockEnabled(),
            runtimeConfig.timeoutSeconds(),
            runtimeConfig.maxTokens(),
            runtimeConfig.temperature()
        );
        return runtimeConfig;
    }

    /**
     * 校验远程模型调用的必要配置。
     *
     * <p>当前闭环已经进入真实模型联调阶段，不再允许 mock 模式、默认 baseUrl、
     * 默认模型名或空密钥引用悄悄兜底；任一配置缺失都必须阻断并输出错误日志。</p>
     *
     * @param model 已启用的模型记录
     * @param secretRef 外部密钥引用名
     * @param baseUrl OpenAI 兼容服务基础地址
     * @param remoteModel 供应商侧模型名称
     * @param mockEnabled 是否启用模拟模式
     * @throws BusinessException 当远程调用配置不完整或仍处于 mock 模式时抛出
     * @author dz
     * @date 2026-06-27
     */
    private void validateRemoteConfig(
        AiModel model,
        String secretRef,
        String baseUrl,
        String remoteModel,
        boolean mockEnabled
    ) {
        if (mockEnabled) {
            failRemoteConfig(model, "AI模型配置仍开启mockEnabled，闭环要求必须调用远程模型");
        }
        if (baseUrl.isBlank()) {
            failRemoteConfig(model, "AI模型baseUrl未配置，不能调用远程模型");
        }
        if (remoteModel.isBlank()) {
            failRemoteConfig(model, "AI模型远端model未配置，不能调用远程模型");
        }
        if (secretRef.isBlank()) {
            failRemoteConfig(model, "AI模型secretRef未配置，不能调用远程模型");
        }
    }

    /**
     * 校验密钥解析结果。
     *
     * @param model 已启用的模型记录
     * @param secretRef 外部密钥引用名
     * @param apiKey 密钥解析器返回的单次调用密钥
     * @throws BusinessException 当密钥为空时抛出
     * @author dz
     * @date 2026-06-27
     */
    private void validateApiKey(AiModel model, String secretRef, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error(
                "AI模型密钥为空: modelCode={}, modelVersion={}, providerCode={}, secretRef={}",
                model.modelCode(),
                model.modelVersion(),
                model.provider(),
                secretRef
            );
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI模型密钥为空: " + secretRef);
        }
    }

    /** 输出远程模型配置错误并抛出业务异常。 */
    private void failRemoteConfig(AiModel model, String reason) {
        log.error(
            "AI远程模型配置不可用: modelCode={}, modelVersion={}, providerCode={}, reason={}",
            model.modelCode(),
            model.modelVersion(),
            model.provider(),
            reason
        );
        throw new BusinessException(HttpStatus.BAD_REQUEST, reason);
    }

    /** 解析外部密钥并记录失败原因，日志不输出密钥明文。 */
    private String resolveSecret(AiModel model, String secretRef) {
        if (secretRef.isBlank()) {
            return "";
        }
        try {
            return secretResolver.resolve(secretRef);
        } catch (BusinessException exception) {
            log.error(
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
