package com.example.dzcom.interfaces.request.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;

/** 我的模拟组合分页请求。 */
@Schema(description = "我的模拟组合分页请求")
public record MockPortfolioListRequest(
    @Schema(description = "页码，从 1 开始；兼容 0 表示第一页")
    Integer page,
    @Schema(description = "每页条数，1-100")
    Integer size,
    @Schema(description = "排序字段：createdAt/updatedAt/portfolioNo/portfolioName")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
