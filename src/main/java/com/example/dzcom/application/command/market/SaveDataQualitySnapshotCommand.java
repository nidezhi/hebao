package com.example.dzcom.application.command.market;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 保存数据质量快照命令。 */
@Builder
@Schema(description = "保存数据质量快照命令")
public record SaveDataQualitySnapshotCommand(
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
    Integer sampleCount,
    @Schema(description = "质量快照时间")
    LocalDateTime snapshotTime,
    @Schema(description = "质量评估上下文和解释 JSON")
    String detail
) {
}
