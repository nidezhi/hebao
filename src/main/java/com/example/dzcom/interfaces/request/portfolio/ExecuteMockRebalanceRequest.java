package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/** 执行模拟再平衡请求。 */
@Schema(description = "执行模拟再平衡请求")
public record ExecuteMockRebalanceRequest(
    @Schema(description = "模拟组合业务唯一标识")
    @NotBlank
    String portfolioBizId,
    @Schema(description = "目标产品权重集合")
    @NotEmpty
    List<@Valid TargetWeightRequest> targets,
    @Schema(description = "最小调仓金额，低于该金额的差额不生成订单")
    BigDecimal minTradeAmount,
    @Schema(description = "客户端幂等键前缀，用于避免重复提交")
    String idempotencyKey
) {
    /** 单个产品目标权重请求。 */
    @Schema(description = "单个产品目标权重请求")
    public record TargetWeightRequest(
        @Schema(description = "产品业务唯一标识")
        @NotBlank
        String productBizId,
        @Schema(description = "目标权重，0到1之间的小数")
        @NotNull
        BigDecimal targetWeight
    ) {
    }
}
