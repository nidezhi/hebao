package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** 投资分析报告分页请求。 */
@Schema(description = "投资分析报告分页请求")
public record InvestmentAnalysisReportListRequest(
    @Schema(description = "市场范围，默认仅中国大陆", example = "CN_MAINLAND")
    String marketScope,
    @Schema(description = "投资主题编码", example = "AI人工智能")
    String themeCode,
    @Schema(description = "分析提供方编码", example = "LOCAL_RULE")
    String providerCode,
    @Schema(description = "状态：SUCCEEDED/FAILED", example = "SUCCEEDED")
    String status,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：generatedAt/createdAt/providerCode/modelCode/themeCode/status",
        example = "generatedAt")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
