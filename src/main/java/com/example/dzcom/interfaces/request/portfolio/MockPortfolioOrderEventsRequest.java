package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 查询模拟组合订单事件请求。 */
@Schema(description = "查询模拟组合订单事件请求")
public record MockPortfolioOrderEventsRequest(
    @Schema(description = "模拟组合业务唯一标识")
    @NotBlank
    String portfolioBizId,
    @Schema(description = "事件数量上限，默认20，最大100")
    Integer limit
) {
}
