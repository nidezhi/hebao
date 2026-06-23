package com.example.dzcom.application.command.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 创建模拟投资组合命令。 */
@Builder
@Schema(description = "创建模拟投资组合命令")
public record CreateMockPortfolioCommand(
    @Schema(description = "模拟组合名称")
    String portfolioName,
    @Schema(description = "基础计价币种，默认 CNY")
    String baseCurrency,
    @Schema(description = "初始模拟现金，当前阶段只进入首个估值快照")
    BigDecimal initialCash
) {
}
