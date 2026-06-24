package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * 生成投资分析报告的应用层命令。
 *
 * <p>接口层将外部请求转换为该命令，应用服务再根据模型编码查询 ACTIVE 模型配置，
 * 注入外部密钥并选择对应 Provider。命令本身不携带 API Key。</p>
 */
@Builder
@Schema(description = "生成投资分析报告的应用层命令")
public record GenerateInvestmentAnalysisCommand(
    @Schema(description = "可选的提供方一致性校验值；实际提供方由模型表决定",
        example = "OPENAI_COMPATIBLE")
    String providerCode,
    @Schema(description = "AI 模型稳定编码；空值使用 openai-compatible-analysis",
        example = "openai-compatible-analysis")
    String modelCode,
    @Schema(description = "分析覆盖的市场范围，默认中国大陆",
        example = "CN_MAINLAND")
    String marketScope,
    @Schema(description = "投资主题稳定编码；空值表示分析市场范围内全部主题",
        example = "AI人工智能")
    String themeCode,
    @Schema(description = "分析数据回看天数；空值或非正数默认 30 天",
        example = "30")
    Integer lookbackDays,
    @Schema(description = "模拟收益使用的初始资金；空值默认 100000",
        example = "100000")
    BigDecimal initialCapital
) {
}
