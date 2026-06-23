package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 撤销模拟订单请求。 */
@Schema(description = "撤销模拟订单请求")
public record CancelMockOrderRequest(
    @Schema(description = "模拟订单业务唯一标识")
    @NotBlank
    String orderBizId,
    @Schema(description = "撤单原因，前端展示和审计使用")
    String cancelReason
) {
}
