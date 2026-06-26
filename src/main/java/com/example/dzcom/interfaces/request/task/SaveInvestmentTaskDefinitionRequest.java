package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/** 保存投资任务配置请求。 */
@Schema(description = "保存投资任务配置请求")
public record SaveInvestmentTaskDefinitionRequest(
    @NotBlank
    @Schema(description = "稳定任务编码", example = "hot-theme-return")
    String code,
    @NotBlank
    @Schema(description = "任务处理器类型", example = "HOT_THEME_RETURN")
    String type,
    @NotBlank
    @Schema(description = "Spring Cron 表达式", example = "30 */5 * * * *")
    String cron,
    @Schema(description = "Cron 时区", example = "Asia/Shanghai")
    String zone,
    @Schema(description = "是否启用", example = "true")
    Boolean enabled,
    @Schema(description = "任务参数；支持字符串、数字、布尔、对象和数组，对象/数组会在接口层序列化为 JSON 字符串")
    Map<String, Object> parameters,
    @Schema(description = "配置说明", example = "中国大陆热门投资方向收益汇总")
    String description
) {
}
