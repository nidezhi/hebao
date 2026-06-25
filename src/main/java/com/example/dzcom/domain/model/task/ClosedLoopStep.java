package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 自动投资闭环步骤记录领域对象。 */
@Builder(toBuilder = true)
@Schema(description = "自动投资闭环步骤记录领域对象")
public record ClosedLoopStep(
    @Schema(description = "步骤业务唯一标识")
    String bizId,
    @Schema(description = "闭环运行业务标识")
    String runBizId,
    @Schema(description = "步骤编码")
    String stepCode,
    @Schema(description = "步骤展示名称")
    String stepName,
    @Schema(description = "步骤顺序")
    int stepOrder,
    @Schema(description = "步骤状态")
    String stepStatus,
    @Schema(description = "输入摘要 JSON")
    String inputSummary,
    @Schema(description = "输出摘要 JSON")
    String outputSummary,
    @Schema(description = "失败原因")
    String failureReason,
    @Schema(description = "开始时间")
    LocalDateTime startedAt,
    @Schema(description = "完成时间")
    LocalDateTime completedAt,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
}
