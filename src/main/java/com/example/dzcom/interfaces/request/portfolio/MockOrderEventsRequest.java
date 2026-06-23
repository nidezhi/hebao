package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 查询模拟订单事件请求。 */
@Schema(description = "查询模拟订单事件请求")
public record MockOrderEventsRequest(
    @Schema(description = "模拟订单业务唯一标识")
    @NotBlank
    String orderBizId
) {
}
