package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 投资任务执行记录分页请求。 */
@Schema(description = "投资任务执行记录分页请求")
public record TaskExecutionListRequest(
    @Schema(description = "任务编码", example = "investment-news-collection")
    String taskCode,
    @Schema(description = "任务类型", example = "INVESTMENT_NEWS_COLLECTION")
    String taskType,
    @Schema(description = "执行状态：RUNNING/SUCCEEDED/BLOCKED/FAILED", example = "SUCCEEDED")
    String status,
    @Schema(description = "执行开始时间起点", example = "2026-06-16T00:00:00")
    LocalDateTime startedFrom,
    @Schema(description = "执行开始时间终点", example = "2026-06-16T23:59:59")
    LocalDateTime startedTo,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：startedAt/completedAt/createdAt/taskCode/taskType/status",
        example = "startedAt")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
