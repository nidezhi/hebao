package com.example.dzcom.domain.model.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资数据源注册领域对象。 */
@Builder
@Schema(description = "投资数据源注册领域对象")
public record DataSource(
    @Schema(description = "数据源业务唯一标识")
    String bizId,
    @Schema(description = "数据源稳定编码")
    String sourceCode,
    @Schema(description = "数据源展示名称")
    String sourceName,
    @Schema(description = "数据源类型：MARKET/NEWS/ANNOUNCEMENT/RESEARCH/REGULATORY")
    String sourceType,
    @Schema(description = "来源等级：L1-L5")
    String trustLevel,
    @Schema(description = "数据源入口地址或供应商网关")
    String baseUrl,
    @Schema(description = "是否启用采集或展示")
    boolean enabled,
    @Schema(description = "采集频率说明或 cron 表达式")
    String fetchFrequency,
    @Schema(description = "数据源负责人或维护方")
    String owner,
    @Schema(description = "数据源用途说明")
    String description,
    @Schema(description = "创建时间（北京时间）")
    LocalDateTime createdAt,
    @Schema(description = "更新时间（北京时间）")
    LocalDateTime updatedAt,
    @Schema(description = "创建操作者业务ID或系统标识")
    String createdBy,
    @Schema(description = "最后更新操作者业务ID或系统标识")
    String updatedBy
) {
}
