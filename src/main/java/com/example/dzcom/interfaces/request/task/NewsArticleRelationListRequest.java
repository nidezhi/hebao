package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

/** 资讯主题产品关联分页查询请求。 */
@Schema(description = "资讯主题产品关联分页查询请求")
public record NewsArticleRelationListRequest(
    @Schema(description = "资讯业务 ID，来自投资资讯列表返回的 bizId；为空时不按资讯筛选")
    String articleBizId,
    @Schema(description = "投资主题编码；例如 AI人工智能 会转换为 AI人工智能 或配置生成的稳定编码")
    String themeCode,
    @Schema(description = "产品代码；例如 159819、588000；为空时查询主题级和产品级关联")
    String productCode,
    @Schema(description = "关联类型，允许值：KEYWORD_MATCH/MANUAL/MODEL_EXTRACTED", example = "KEYWORD_MATCH")
    String relationType,
    @Schema(description = "页码，从 1 开始；空值默认 1", example = "1")
    Integer page,
    @Schema(description = "每页条数，建议 1-100；空值默认 20", example = "20")
    Integer size,
    @Schema(description = "排序字段：createdAt/relationScore/sourceQualityScore/themeCode/productCode",
        example = "relationScore")
    String sort,
    @Schema(description = "排序方向：asc/desc；空值默认 desc", example = "desc")
    String direction
) {
}
