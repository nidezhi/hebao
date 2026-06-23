package com.example.dzcom.interfaces.request.market;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存数据源请求。 */
@Schema(description = "保存数据源请求")
public record SaveDataSourceRequest(
    @Schema(description = "数据源稳定编码")
    @NotBlank
    String sourceCode,
    @Schema(description = "数据源展示名称")
    @NotBlank
    String sourceName,
    @Schema(description = "数据源类型：MARKET/NEWS/ANNOUNCEMENT/RESEARCH/REGULATORY/FALLBACK")
    @NotBlank
    String sourceType,
    @Schema(description = "来源等级：L1-L5")
    @NotBlank
    String trustLevel,
    @Schema(description = "数据源入口地址")
    String baseUrl,
    @Schema(description = "是否启用")
    Boolean enabled,
    @Schema(description = "采集频率说明或 cron 表达式")
    String fetchFrequency,
    @Schema(description = "负责人或维护方")
    String owner,
    @Schema(description = "数据源用途说明")
    String description
) {
}
