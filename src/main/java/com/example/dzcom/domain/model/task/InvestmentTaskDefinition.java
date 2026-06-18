package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/** 可通过数据库和接口管理的投资定时任务定义。 */
@Builder
@Schema(description = "投资定时任务定义领域对象")
public record InvestmentTaskDefinition(
    @Schema(description = "任务配置业务唯一标识")
    String bizId,
    @Schema(description = "稳定任务编码")
    String taskCode,
    @Schema(description = "用于匹配处理器的任务类型")
    String taskType,
    @Schema(description = "Spring Cron 表达式")
    String cron,
    @Schema(description = "Cron 计算时区")
    String zone,
    @Schema(description = "是否启用动态调度")
    boolean enabled,
    @Schema(description = "由具体处理器解释的任务参数")
    Map<String, String> parameters,
    @Schema(description = "任务用途和配置说明")
    String description,
    @Schema(description = "配置创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "配置最后更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
