package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 投资资讯分页请求。 */
@Schema(description = "投资资讯分页请求")
public record NewsArticleListRequest(
    @Schema(description = "标题或摘要关键字", example = "AI")
    String keyword,
    @Schema(description = "资讯类型", example = "NEWS")
    String articleType,
    @Schema(description = "来源编码", example = "CNINFO")
    String sourceCode,
    @Schema(description = "语言编码", example = "zh-CN")
    String languageCode,
    @Schema(description = "发布时间起点", example = "2026-06-16T00:00:00")
    LocalDateTime publishFrom,
    @Schema(description = "发布时间终点", example = "2026-06-16T23:59:59")
    LocalDateTime publishTo,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：publishTime/collectedAt/createdAt/title/sourceCode",
        example = "publishTime")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
