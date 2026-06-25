package com.example.dzcom.application.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 自动投资闭环运行视图。 */
@Builder
@Schema(description = "自动投资闭环运行视图")
public record ClosedLoopRunView(
    @Schema(description = "闭环运行业务唯一标识")
    String bizId,
    @Schema(description = "运行编号")
    String runNo,
    @Schema(description = "任务编码")
    String taskCode,
    @Schema(description = "触发来源")
    String triggerSource,
    @Schema(description = "运行状态")
    String runStatus,
    @Schema(description = "自动化等级")
    String automationLevel,
    @Schema(description = "市场范围")
    String marketScope,
    @Schema(description = "主题编码")
    String themeCode,
    @Schema(description = "自动 Mock 用户业务标识")
    String mockUserBizId,
    @Schema(description = "Mock 组合业务标识")
    String portfolioBizId,
    @Schema(description = "报告业务标识")
    String reportBizId,
    @Schema(description = "Prompt 业务标识")
    String promptBizId,
    @Schema(description = "Prompt 编码")
    String promptCode,
    @Schema(description = "Prompt 版本")
    String promptVersion,
    @Schema(description = "回测业务标识")
    String backtestBizId,
    @Schema(description = "质量分")
    BigDecimal qualityScore,
    @Schema(description = "门禁结果")
    String gateResult,
    @Schema(description = "运行摘要 JSON")
    String summary,
    @Schema(description = "失败原因")
    String failureReason,
    @Schema(description = "开始时间")
    LocalDateTime startedAt,
    @Schema(description = "完成时间")
    LocalDateTime completedAt,
    @Schema(description = "步骤记录")
    List<ClosedLoopStepView> steps
) {
}
