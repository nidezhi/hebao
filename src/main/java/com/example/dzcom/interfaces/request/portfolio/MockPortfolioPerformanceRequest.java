package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 模拟组合收益曲线请求。 */
@Schema(description = "模拟组合收益曲线请求")
public record MockPortfolioPerformanceRequest(
    @Schema(description = "模拟组合业务唯一标识")
    @NotBlank
    String portfolioBizId,
    @Schema(description = "收益曲线点数量上限，1-500，默认120")
    Integer limit
) {
}
