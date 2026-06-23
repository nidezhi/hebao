package com.example.dzcom.domain.model.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 数据源当前健康状态领域对象。 */
@Builder
@Schema(description = "数据源当前健康状态领域对象")
public record DataSourceHealth(
    @Schema(description = "健康状态业务唯一标识")
    String bizId,
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
    @Schema(description = "更新时间（北京时间）")
    LocalDateTime updatedAt
) {
}
