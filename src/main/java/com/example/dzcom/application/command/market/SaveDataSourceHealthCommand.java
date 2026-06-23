package com.example.dzcom.application.command.market;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 保存数据源健康状态命令。 */
@Builder
@Schema(description = "保存数据源健康状态命令")
public record SaveDataSourceHealthCommand(
    @Schema(description = "数据源稳定编码")
    String sourceCode,
    @Schema(description = "最近成功采集时间")
    LocalDateTime lastSuccessAt,
    @Schema(description = "最近失败时间")
    LocalDateTime lastFailureAt,
    @Schema(description = "近期成功率，0-1")
    BigDecimal successRate,
    @Schema(description = "平均响应耗时毫秒")
    Integer avgLatencyMs,
    @Schema(description = "最近失败原因摘要")
    String failureReason,
    @Schema(description = "最近窗口样本数量")
    Integer sampleCount
) {
}
