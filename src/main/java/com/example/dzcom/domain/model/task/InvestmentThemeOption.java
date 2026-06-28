package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 从真实主题快照中提炼出的投资主题选择器选项。 */
@Builder
@Schema(description = "投资主题选择器选项")
public record InvestmentThemeOption(
    @Schema(description = "投资主题稳定编码")
    String themeCode,
    @Schema(description = "投资主题展示名称")
    String themeName,
    @Schema(description = "市场范围")
    String marketScope,
    @Schema(description = "最近快照类型")
    String latestSnapshotType,
    @Schema(description = "最近快照时间")
    LocalDateTime latestSnapshotTime,
    @Schema(description = "最近样本数量")
    int sampleCount,
    @Schema(description = "最近收益率")
    BigDecimal returnRate,
    @Schema(description = "最近动量分数")
    BigDecimal momentumScore,
    @Schema(description = "最近热度分数")
    BigDecimal heatScore
) {
}
