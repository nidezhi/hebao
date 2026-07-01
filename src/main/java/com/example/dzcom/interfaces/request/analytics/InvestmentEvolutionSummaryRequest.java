package com.example.dzcom.interfaces.request.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 投资闭环持续进化分析请求，控制后端按最近多少条业务样本做指标归集。 */
public record InvestmentEvolutionSummaryRequest(
    @Schema(description = "样本窗口大小，默认100，最大100")
    @Min(value = 1, message = "sampleSize不能小于1")
    @Max(value = 100, message = "sampleSize不能大于100")
    Integer sampleSize
) {
}
