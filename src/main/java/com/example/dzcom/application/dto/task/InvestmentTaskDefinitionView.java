package com.example.dzcom.application.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Map;

/** 投资任务配置视图。 */
@Builder
@Schema(description = "投资任务配置应用层视图")
public record InvestmentTaskDefinitionView(
    @Schema(description = "稳定任务编码")
    String code,
    @Schema(description = "任务处理器类型")
    String type,
    @Schema(description = "Spring Cron 表达式")
    String cron,
    @Schema(description = "Cron 计算时区")
    String zone,
    @Schema(description = "是否启用动态调度")
    boolean enabled,
    @Schema(description = "由具体任务处理器解释的参数集合")
    Map<String, String> parameters,
    @Schema(description = "任务用途和配置说明")
    String description
) {
}
