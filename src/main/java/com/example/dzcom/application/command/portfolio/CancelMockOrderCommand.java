package com.example.dzcom.application.command.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 撤销模拟订单命令。 */
@Builder
@Schema(description = "撤销模拟订单命令")
public record CancelMockOrderCommand(
    @Schema(description = "模拟订单业务唯一标识")
    String orderBizId,
    @Schema(description = "撤单原因，前端展示和审计使用")
    String cancelReason
) {
}
