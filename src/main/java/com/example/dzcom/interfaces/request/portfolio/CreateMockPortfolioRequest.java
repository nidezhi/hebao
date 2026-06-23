package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/** 创建模拟组合请求。 */
@Schema(description = "创建模拟组合请求")
public record CreateMockPortfolioRequest(
    @Schema(description = "模拟组合名称")
    @NotBlank
    String portfolioName,
    @Schema(description = "基础计价币种，默认 CNY")
    String baseCurrency,
    @Schema(description = "初始模拟现金，当前阶段写入首个估值快照")
    BigDecimal initialCash
) {
}
