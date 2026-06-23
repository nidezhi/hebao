package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 从模拟组合估值曲线生成回测摘要命令。 */
@Builder
@Schema(description = "从模拟组合估值曲线生成回测摘要命令")
public record GenerateBacktestFromPortfolioCommand(
    @Schema(description = "模拟组合业务唯一标识")
    String portfolioBizId,
    @Schema(description = "策略稳定编码")
    String strategyCode,
    @Schema(description = "策略版本快照")
    String strategyVersion,
    @Schema(description = "基准指数或比较对象编码")
    String benchmarkCode,
    @Schema(description = "回测参数 JSON")
    String parameters,
    @Schema(description = "估值曲线点数量上限")
    Integer limit
) {
}
