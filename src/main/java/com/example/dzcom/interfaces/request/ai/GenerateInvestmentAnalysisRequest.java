package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** 生成投资分析报告请求。 */
@Schema(description = "生成投资分析报告请求")
public record GenerateInvestmentAnalysisRequest(
    @Schema(description = "分析提供方编码；默认 LOCAL_RULE", example = "LOCAL_RULE")
    String providerCode,
    @Schema(description = "模型编码或本地分析器编码", example = "local-rule-v1")
    String modelCode,
    @Schema(description = "市场范围，默认仅中国大陆", example = "CN_MAINLAND")
    String marketScope,
    @Schema(description = "投资主题编码；为空时分析全部中国大陆主题", example = "AI人工智能")
    String themeCode,
    @Schema(description = "回看天数，默认 30", example = "30")
    Integer lookbackDays,
    @DecimalMin("0")
    @Schema(description = "模拟收益初始资金，默认 100000", example = "100000")
    BigDecimal initialCapital
) {
}
