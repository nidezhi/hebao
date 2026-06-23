package com.example.dzcom.application.dto.market;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 数据源健康状态应用层视图。 */
@Builder
@Schema(description = "数据源健康状态应用层视图")
public record DataSourceHealthView(
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
    int sampleCount,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
}
