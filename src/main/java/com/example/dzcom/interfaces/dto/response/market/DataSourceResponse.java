package com.example.dzcom.interfaces.dto.response.market;

import com.example.dzcom.application.dto.market.DataSourceView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 数据源治理响应。 */
@Builder
@Schema(description = "数据源治理响应")
public record DataSourceResponse(
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
    DataSourceHealthResponse health,
    @Schema(description = "最新质量快照")
    DataQualitySnapshotResponse latestQuality,
    @Schema(description = "质量等级")
    String qualityLevel,
    @Schema(description = "前端展示提示文案")
    String displayMessage,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
    /** 从应用层视图转换为接口响应。 */
    public static DataSourceResponse from(DataSourceView view) {
        return DataSourceResponse.builder()
            .bizId(view.bizId())
            .sourceCode(view.sourceCode())
            .sourceName(view.sourceName())
            .sourceType(view.sourceType())
            .trustLevel(view.trustLevel())
            .baseUrl(view.baseUrl())
            .enabled(view.enabled())
            .fetchFrequency(view.fetchFrequency())
            .owner(view.owner())
            .description(view.description())
            .health(DataSourceHealthResponse.from(view.health()))
            .latestQuality(DataQualitySnapshotResponse.from(view.latestQuality()))
            .qualityLevel(view.qualityLevel())
            .displayMessage(view.displayMessage())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .build();
    }
}
