package com.example.dzcom.infrastructure.config.task;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 投资任务配置属性注册。 */
@Configuration
@EnableConfigurationProperties(InvestmentTaskProperties.class)
public class InvestmentTaskPropertiesConfiguration {
}
