package com.example.dzcom.application.command.ai;

import lombok.Builder;

import java.math.BigDecimal;

/** 生成投资分析报告命令。 */
@Builder
public record GenerateInvestmentAnalysisCommand(
    String providerCode,
    String modelCode,
    String marketScope,
    String themeCode,
    Integer lookbackDays,
    BigDecimal initialCapital
) {
}
