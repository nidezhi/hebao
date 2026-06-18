package com.example.dzcom.infrastructure.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 模型外部密钥配置。
 *
 * <p>Map 的 key 是模型表中的 {@code secretRef}，value 是环境变量、
 * 本地私密配置或部署平台 Secret 注入的实际 API Key。</p>
 */
@Data
@ConfigurationProperties(prefix = "ai.secrets")
public class AiSecretProperties {
    /** 密钥引用名到实际 API Key 的映射。 */
    private Map<String, String> values = new LinkedHashMap<>();
}
