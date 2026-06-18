package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.ai.AiSecretResolver;
import com.example.dzcom.infrastructure.config.ai.AiSecretProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** 从 Spring 外部配置中解析 AI 模型 API Key。 */
@Component
@RequiredArgsConstructor
public class PropertyAiSecretResolver implements AiSecretResolver {
    private final AiSecretProperties properties;

    /**
     * 根据密钥引用名读取外部注入的 API Key。
     *
     * @param secretRef 模型配置声明的密钥引用名
     * @return 非空 API Key
     * @throws BusinessException 当引用名为空或未配置对应密钥时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public String resolve(String secretRef) {
        if (secretRef == null || secretRef.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI模型未配置密钥引用");
        }
        String apiKey = properties.getValues().get(secretRef);
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI模型密钥未配置: " + secretRef
            );
        }
        return apiKey;
    }
}
