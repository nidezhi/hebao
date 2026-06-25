package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.application.dto.task.ClosedLoopStepView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 自动投资闭环步骤响应。 */
@Builder
@Schema(description = "自动投资闭环步骤响应")
public record ClosedLoopStepResponse(
    @Schema(description = "步骤业务唯一标识") String bizId,
    @Schema(description = "步骤编码") String stepCode,
    @Schema(description = "步骤名称") String stepName,
    @Schema(description = "步骤顺序") int stepOrder,
    @Schema(description = "步骤状态：SUCCEEDED/SKIPPED/BLOCKED/FAILED") String stepStatus,
    @Schema(description = "输入摘要 JSON") String inputSummary,
    @Schema(description = "输出摘要 JSON") String outputSummary,
    @Schema(description = "失败或阻断原因") String failureReason,
    @Schema(description = "开始时间") LocalDateTime startedAt,
    @Schema(description = "完成时间") LocalDateTime completedAt
) {
    /** 将应用层视图转换为接口响应。 */
    public static ClosedLoopStepResponse from(ClosedLoopStepView view) {
        return ClosedLoopStepResponse.builder()
            .bizId(view.bizId())
            .stepCode(view.stepCode())
            .stepName(view.stepName())
            .stepOrder(view.stepOrder())
            .stepStatus(view.stepStatus())
            .inputSummary(view.inputSummary())
            .outputSummary(view.outputSummary())
            .failureReason(view.failureReason())
            .startedAt(view.startedAt())
            .completedAt(view.completedAt())
            .build();
    }
}
