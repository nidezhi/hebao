package com.example.dzcom.domain.model.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 数据源质量历史快照领域对象。 */
@Builder
@Schema(description = "数据源质量历史快照领域对象")
public record DataQualitySnapshot(
    @Schema(description = "数据质量快照业务唯一标识")
    String bizId,
    @Schema(description = "数据源稳定编码")
    String sourceCode,
    @Schema(description = "数据类型：MARKET_QUOTE/NEWS/ANNOUNCEMENT/RESEARCH")
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
    String detail,
    @Schema(description = "创建时间（北京时间）")
    LocalDateTime createdAt
) {
}
