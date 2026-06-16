package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/** 手动触发投资任务请求。 */
@Schema(description = "手动触发投资任务请求")
public record TriggerInvestmentTaskRequest(
    @NotBlank
    @Schema(description = "任务编码，必须存在于 investment.tasks.definitions 配置中",
        example = "investment-news-collection")
    String taskCode,
    @Schema(description = "本次手动触发覆盖参数；为空时使用配置默认参数")
    Map<String, String> parameters
) {
}
