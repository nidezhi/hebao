package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.infrastructure.config.ai.AiSecretProperties;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** AI 模型运行配置和外部密钥注入测试。 */
class JacksonAiModelRuntimeConfigResolverTest {
    /**
     * 模型配置中的 secretRef 应解析为外部 UUID 模拟 API Key。
     *
     * @author dz
     * @date 2026-06-18
     */
    @Test
    void shouldResolveModelConfigAndUuidApiKey() {
        String mockApiKey = "83ec6f52-567b-40a7-87ad-8b60df196d6c";
        AiSecretProperties properties = new AiSecretProperties();
        properties.setValues(Map.of("OPENAI_MOCK_API_KEY", mockApiKey));
        PropertyAiSecretResolver secretResolver = new PropertyAiSecretResolver(properties);
        JacksonAiModelRuntimeConfigResolver resolver =
            new JacksonAiModelRuntimeConfigResolver(
                JsonMapper.builder().findAndAddModules().build(),
                secretResolver
            );
        AiModel model = AiModel.builder()
            .modelCode("openai-compatible-analysis")
            .modelVersion("mock-v1")
            .provider("OPENAI_COMPATIBLE")
            .modelConfig("""
                {
                  "baseUrl": "https://api.openai.com/v1",
                  "model": "gpt-4.1-mini",
                  "secretRef": "OPENAI_MOCK_API_KEY",
                  "timeoutSeconds": 60,
                  "temperature": 0.2,
                  "mockEnabled": true
                }
                """)
            .build();

        AiModelRuntimeConfig runtimeConfig = resolver.resolve(model);

        assertEquals(mockApiKey, runtimeConfig.apiKey());
        assertEquals("OPENAI_MOCK_API_KEY", runtimeConfig.secretRef());
        assertEquals("gpt-4.1-mini", runtimeConfig.remoteModel());
        assertEquals(60, runtimeConfig.timeoutSeconds());
        assertTrue(runtimeConfig.mockEnabled());
    }
}
