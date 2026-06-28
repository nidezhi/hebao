package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

/** 投资主题选择器查询请求。 */
@Schema(description = "投资主题选择器查询请求")
public record InvestmentThemeOptionListRequest(
    @Schema(description = "关键字，匹配主题编码或主题名称", example = "AI")
    String keyword,
    @Schema(description = "市场范围，默认仅中国大陆", example = "CN_MAINLAND")
    String marketScope,
    @Schema(description = "页码，从 1 开始", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size
) {
}
