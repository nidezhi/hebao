package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 保存回测结果请求。 */
@Schema(description = "保存回测结果请求")
public record SaveBacktestResultRequest(
    @Schema(description = "回测结果业务唯一标识，更新时传入")
    String bizId,
    @NotBlank
    @Schema(description = "策略稳定编码")
    String strategyCode,
    @NotBlank
    @Schema(description = "策略版本快照")
    String strategyVersion,
    @NotNull
    @Schema(description = "回测区间开始日期")
    LocalDate startDate,
    @NotNull
    @Schema(description = "回测区间结束日期")
    LocalDate endDate,
    @NotNull
    @Positive
    @Schema(description = "初始资金")
    BigDecimal initialCapital,
    @Schema(description = "基准指数或比较对象编码")
    String benchmarkCode,
    @NotBlank
    @Schema(description = "回测参数 JSON")
    String parameters,
    @Schema(description = "回测指标 JSON")
    String metrics,
    @Schema(description = "明细结果或报告存储地址")
    String resultUri,
    @Schema(description = "回测状态：PENDING/RUNNING/SUCCEEDED/FAILED/CANCELLED")
    String status,
    @Schema(description = "失败原因摘要")
    String failureReason,
    @Schema(description = "任务开始时间")
    LocalDateTime startedAt,
    @Schema(description = "任务完成时间")
    LocalDateTime completedAt
) {
}
