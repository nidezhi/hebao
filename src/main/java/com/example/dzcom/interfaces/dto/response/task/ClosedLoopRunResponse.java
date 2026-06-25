package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.application.dto.task.ClosedLoopRunView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 自动投资闭环运行响应。 */
@Builder
@Schema(description = "自动投资闭环运行响应")
public record ClosedLoopRunResponse(
    @Schema(description = "闭环运行业务唯一标识") String bizId,
    @Schema(description = "运行编号") String runNo,
    @Schema(description = "任务编码") String taskCode,
    @Schema(description = "触发来源：SCHEDULE/MANUAL/RETRY") String triggerSource,
    @Schema(description = "运行状态：RUNNING/SUCCEEDED/BLOCKED/FAILED") String runStatus,
    @Schema(description = "自动化等级：DATA_ONLY/MOCK_ONLY/FULL_MOCK") String automationLevel,
    @Schema(description = "市场范围") String marketScope,
    @Schema(description = "主题编码") String themeCode,
    @Schema(description = "自动 Mock 用户业务标识") String mockUserBizId,
    @Schema(description = "Mock 组合业务标识") String portfolioBizId,
    @Schema(description = "报告业务标识") String reportBizId,
    @Schema(description = "Prompt 业务标识") String promptBizId,
    @Schema(description = "Prompt 编码") String promptCode,
    @Schema(description = "Prompt 版本") String promptVersion,
    @Schema(description = "回测业务标识") String backtestBizId,
    @Schema(description = "质量分") BigDecimal qualityScore,
    @Schema(description = "门禁结果：PENDING/PASS/BLOCK") String gateResult,
    @Schema(description = "运行摘要 JSON") String summary,
    @Schema(description = "失败或阻断原因") String failureReason,
    @Schema(description = "开始时间") LocalDateTime startedAt,
    @Schema(description = "完成时间") LocalDateTime completedAt,
    @Schema(description = "步骤记录") List<ClosedLoopStepResponse> steps
) {
    /** 将应用层视图转换为接口响应。 */
    public static ClosedLoopRunResponse from(ClosedLoopRunView view) {
        return ClosedLoopRunResponse.builder()
            .bizId(view.bizId())
            .runNo(view.runNo())
            .taskCode(view.taskCode())
            .triggerSource(view.triggerSource())
            .runStatus(view.runStatus())
            .automationLevel(view.automationLevel())
            .marketScope(view.marketScope())
            .themeCode(view.themeCode())
            .mockUserBizId(view.mockUserBizId())
            .portfolioBizId(view.portfolioBizId())
            .reportBizId(view.reportBizId())
            .promptBizId(view.promptBizId())
            .promptCode(view.promptCode())
            .promptVersion(view.promptVersion())
            .backtestBizId(view.backtestBizId())
            .qualityScore(view.qualityScore())
            .gateResult(view.gateResult())
            .summary(view.summary())
            .failureReason(view.failureReason())
            .startedAt(view.startedAt())
            .completedAt(view.completedAt())
            .steps(view.steps() == null ? List.of() : view.steps().stream()
                .map(ClosedLoopStepResponse::from)
                .toList())
            .build();
    }
}
