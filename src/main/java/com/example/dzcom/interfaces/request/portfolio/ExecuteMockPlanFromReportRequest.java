package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 从投资分析报告执行模拟买入请求。 */
@Schema(description = "从投资分析报告执行模拟买入请求")
public record ExecuteMockPlanFromReportRequest(
    @Schema(description = "模拟组合业务唯一标识")
    @NotBlank
    String portfolioBizId,
    @Schema(description = "投资分析报告业务唯一标识")
    @NotBlank
    String reportBizId,
    @Schema(description = "可选产品业务唯一标识；为空时按报告主题自动选择产品关系")
    String productBizId,
    @Schema(description = "客户端幂等键，用于避免重复提交")
    String idempotencyKey
) {
}
