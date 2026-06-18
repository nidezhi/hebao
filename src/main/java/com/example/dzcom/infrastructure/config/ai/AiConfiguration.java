package com.example.dzcom.infrastructure.config.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 注册 AI 模型密钥和运行配置相关属性。 */
@Configuration
@EnableConfigurationProperties(AiSecretProperties.class)
public class AiConfiguration {
}
