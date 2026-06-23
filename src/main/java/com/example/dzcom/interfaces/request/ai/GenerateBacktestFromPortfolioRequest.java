package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 从模拟组合生成回测摘要请求。 */
@Schema(description = "从模拟组合生成回测摘要请求")
public record GenerateBacktestFromPortfolioRequest(
    @NotBlank
    @Schema(description = "模拟组合业务唯一标识")
    String portfolioBizId,
    @NotBlank
    @Schema(description = "策略稳定编码")
    String strategyCode,
    @NotBlank
    @Schema(description = "策略版本快照")
    String strategyVersion,
    @Schema(description = "基准指数或比较对象编码")
    String benchmarkCode,
    @Schema(description = "回测参数 JSON，可为空，由后端补充组合来源")
    String parameters,
    @Schema(description = "估值曲线点数量上限")
    Integer limit
) {
}
