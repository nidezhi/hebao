package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 投资方向收益与热度快照分页请求。 */
@Schema(description = "投资方向收益与热度快照分页请求")
public record InvestmentThemeSnapshotListRequest(
    @Schema(description = "任务编码", example = "hot-theme-return")
    String taskCode,
    @Schema(description = "快照类型：RETURN/MOMENTUM/HEAT", example = "RETURN")
    String snapshotType,
    @Schema(description = "投资主题编码", example = "AI")
    String themeCode,
    @Schema(description = "市场范围，默认仅中国大陆", example = "CN_MAINLAND")
    String marketScope,
    @Schema(description = "快照时间起点", example = "2026-06-16T00:00:00")
    LocalDateTime snapshotFrom,
    @Schema(description = "快照时间终点", example = "2026-06-16T23:59:59")
    LocalDateTime snapshotTo,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：snapshotTime/createdAt/taskCode/snapshotType/themeCode/returnRate/momentumScore/heatScore",
        example = "snapshotTime")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
