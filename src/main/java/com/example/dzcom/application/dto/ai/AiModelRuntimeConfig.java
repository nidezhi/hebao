package com.example.dzcom.application.dto.ai;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * AI 模型单次调用所需的运行时配置。
 *
 * <p>普通连接参数来自 {@code aiw_ai_model.model_config}，API Key 由
 * {@code secretRef} 通过外部密钥解析器取得，不在数据库中保存明文。</p>
 */
@Builder
public record AiModelRuntimeConfig(
    String modelCode,
    String modelVersion,
    String providerCode,
    String baseUrl,
    String remoteModel,
    String secretRef,
    String apiKey,
    int timeoutSeconds,
    BigDecimal temperature,
    boolean mockEnabled
) {
}
