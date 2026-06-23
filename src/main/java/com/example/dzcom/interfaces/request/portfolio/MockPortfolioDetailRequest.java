package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 模拟组合详情请求。 */
@Schema(description = "模拟组合详情请求")
public record MockPortfolioDetailRequest(
    @Schema(description = "模拟组合业务唯一标识")
    @NotBlank
    String portfolioBizId
) {
}
