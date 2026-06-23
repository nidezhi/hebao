package com.example.dzcom.application.command.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 保存回测结果命令。 */
@Builder
@Schema(description = "保存回测结果命令")
public record SaveBacktestResultCommand(
    @Schema(description = "回测结果业务唯一标识，更新时传入")
    String bizId,
    @Schema(description = "策略稳定编码")
    String strategyCode,
    @Schema(description = "策略版本快照")
    String strategyVersion,
    @Schema(description = "回测区间开始日期")
    LocalDate startDate,
    @Schema(description = "回测区间结束日期")
    LocalDate endDate,
    @Schema(description = "初始资金")
    BigDecimal initialCapital,
    @Schema(description = "基准指数或比较对象编码")
    String benchmarkCode,
    @Schema(description = "回测参数 JSON")
    String parameters,
    @Schema(description = "回测指标 JSON")
    String metrics,
    @Schema(description = "明细结果或报告存储地址")
    String resultUri,
    @Schema(description = "回测状态")
    String status,
    @Schema(description = "失败原因摘要")
    String failureReason,
    @Schema(description = "任务开始时间")
    LocalDateTime startedAt,
    @Schema(description = "任务完成时间")
    LocalDateTime completedAt
) {
}
