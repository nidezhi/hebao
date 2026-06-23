package com.example.dzcom.application.dto.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 数据源治理应用层视图。 */
@Builder
@Schema(description = "数据源治理应用层视图")
public record DataSourceView(
    @Schema(description = "数据源业务唯一标识")
    String bizId,
    @Schema(description = "数据源稳定编码")
    String sourceCode,
    @Schema(description = "数据源展示名称")
    String sourceName,
    @Schema(description = "数据源类型")
    String sourceType,
    @Schema(description = "来源等级")
    String trustLevel,
    @Schema(description = "数据源入口地址")
    String baseUrl,
    @Schema(description = "是否启用")
    boolean enabled,
    @Schema(description = "采集频率说明或 cron 表达式")
    String fetchFrequency,
    @Schema(description = "负责人或维护方")
    String owner,
    @Schema(description = "数据源用途说明")
    String description,
    @Schema(description = "健康状态")
    DataSourceHealthView health,
    @Schema(description = "最新质量快照")
    DataQualitySnapshotView latestQuality,
    @Schema(description = "质量等级")
    String qualityLevel,
    @Schema(description = "前端展示提示文案")
    String displayMessage,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
}
