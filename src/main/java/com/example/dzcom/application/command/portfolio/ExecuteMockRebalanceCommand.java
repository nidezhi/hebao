package com.example.dzcom.application.command.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** 执行模拟再平衡命令。 */
@Builder
@Schema(description = "执行模拟再平衡命令")
public record ExecuteMockRebalanceCommand(
    @Schema(description = "模拟组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "目标产品权重集合")
    List<TargetWeight> targets,
    @Schema(description = "最小调仓金额，低于该金额的差额不生成订单")
    BigDecimal minTradeAmount,
    @Schema(description = "客户端幂等键前缀，用于避免重复提交")
    String idempotencyKey
) {
    /** 单个产品目标权重。 */
    @Builder
    @Schema(description = "单个产品目标权重")
    public record TargetWeight(
        @Schema(description = "产品业务唯一标识")
        String productBizId,
        @Schema(description = "目标权重，0到1之间的小数")
        BigDecimal targetWeight
    ) {
    }
}
