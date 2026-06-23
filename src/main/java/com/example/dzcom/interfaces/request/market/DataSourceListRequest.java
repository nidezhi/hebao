package com.example.dzcom.interfaces.request.market;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 数据源列表请求。 */
@Schema(description = "数据源列表请求")
public record DataSourceListRequest(
    @Schema(description = "关键字，匹配编码或名称")
    String keyword,
    @Schema(description = "数据源类型筛选")
    String sourceType,
    @Schema(description = "来源等级筛选")
    String trustLevel,
    @Schema(description = "启用状态筛选")
    Boolean enabled,
    @Schema(description = "页码，从1开始")
    @Min(1)
    Integer page,
    @Schema(description = "每页数量")
    @Min(1) @Max(200)
    Integer size,
    @Schema(description = "排序字段")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
