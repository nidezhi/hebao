package com.example.dzcom.application.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * AI 模型单次调用所需的运行时配置。
 *
 * <p>普通连接参数来自 {@code aiw_ai_model.model_config}，API Key 由
 * {@code secretRef} 通过外部密钥解析器取得，不在数据库中保存明文。</p>
 */
@Builder
@Schema(description = "AI 模型单次调用运行时配置")
public record AiModelRuntimeConfig(
    @Schema(description = "模型稳定编码")
    String modelCode,
    @Schema(description = "当前启用模型版本")
    String modelVersion,
    @Schema(description = "模型 Provider 编码")
    String providerCode,
    @Schema(description = "模型服务基础地址")
    String baseUrl,
    @Schema(description = "供应商侧远端模型名称")
    String remoteModel,
    @Schema(description = "外部密钥引用名")
    String secretRef,
    @Schema(description = "仅存在于单次调用内存中的 API Key，禁止持久化、响应和日志输出",
        accessMode = Schema.AccessMode.READ_ONLY)
    String apiKey,
    @Schema(description = "模型请求超时时间，单位秒")
    int timeoutSeconds,
    @Schema(description = "模型最大输出 token 数；小于等于0表示不显式限制")
    int maxTokens,
    @Schema(description = "模型生成温度参数")
    BigDecimal temperature,
    @Schema(description = "是否启用模拟调用模式")
    boolean mockEnabled
) {
}
