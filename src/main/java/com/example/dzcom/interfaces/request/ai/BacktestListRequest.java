package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** 回测结果分页查询请求。 */
@Schema(description = "回测结果分页查询请求")
public record BacktestListRequest(
    @Schema(description = "策略稳定编码")
    String strategyCode,
    @Schema(description = "策略版本快照")
    String strategyVersion,
    @Schema(description = "回测状态")
    String status,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页")
    Integer page,
    @Schema(description = "每页条数，1-100")
    Integer size,
    @Schema(description = "排序字段：createdAt/strategyCode/strategyVersion/status/startDate/endDate")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
