package com.example.dzcom.application.dto.market;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 数据质量快照应用层视图。 */
@Builder
@Schema(description = "数据质量快照应用层视图")
public record DataQualitySnapshotView(
    @Schema(description = "数据质量快照业务唯一标识")
    String bizId,
    @Schema(description = "数据源稳定编码")
    String sourceCode,
    @Schema(description = "数据类型")
    String dataType,
    @Schema(description = "综合质量分，0-1")
    BigDecimal qualityScore,
    @Schema(description = "缺失率，0-1")
    BigDecimal missingRate,
    @Schema(description = "重复率，0-1")
    BigDecimal duplicateRate,
    @Schema(description = "新鲜度分，0-1")
    BigDecimal freshnessScore,
    @Schema(description = "参与评估样本数")
    int sampleCount,
    @Schema(description = "质量快照时间")
    LocalDateTime snapshotTime,
    @Schema(description = "质量评估上下文和解释 JSON")
    String detail
) {
}
