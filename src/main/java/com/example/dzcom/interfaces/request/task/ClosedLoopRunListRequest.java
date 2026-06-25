package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 自动投资闭环运行分页请求。 */
@Schema(description = "自动投资闭环运行分页请求")
public record ClosedLoopRunListRequest(
    @Schema(description = "任务编码", example = "auto-investment-closed-loop-orchestration")
    String taskCode,
    @Schema(description = "运行状态：RUNNING/SUCCEEDED/BLOCKED/FAILED", example = "SUCCEEDED")
    String runStatus,
    @Schema(description = "自动化等级：DATA_ONLY/MOCK_ONLY/FULL_MOCK", example = "FULL_MOCK")
    String automationLevel,
    @Schema(description = "市场范围", example = "CN_MAINLAND")
    String marketScope,
    @Schema(description = "主题编码", example = "AI人工智能")
    String themeCode,
    @Schema(description = "自动 Mock 用户业务标识")
    String mockUserBizId,
    @Schema(description = "运行开始时间起点", example = "2026-06-25T00:00:00")
    LocalDateTime startedFrom,
    @Schema(description = "运行开始时间终点", example = "2026-06-25T23:59:59")
    LocalDateTime startedTo,
    @Schema(description = "页码，从 1 开始", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：startedAt/completedAt/updatedAt/runNo/taskCode/runStatus/automationLevel/qualityScore",
        example = "startedAt")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
