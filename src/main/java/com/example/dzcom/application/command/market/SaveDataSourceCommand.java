package com.example.dzcom.application.command.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 保存数据源注册信息命令。 */
@Builder
@Schema(description = "保存数据源注册信息命令")
public record SaveDataSourceCommand(
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
    Boolean enabled,
    @Schema(description = "采集频率说明或 cron 表达式")
    String fetchFrequency,
    @Schema(description = "负责人或维护方")
    String owner,
    @Schema(description = "数据源用途说明")
    String description
) {
}
