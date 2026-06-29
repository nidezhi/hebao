package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** 复盘闭环结构化元数据请求。 */
@Schema(description = "复盘闭环结构化元数据请求")
public record ReviewLoopMetadataRequest(
    @Schema(description = "前端页面或调用场景，可为空", example = "review-loop")
    String scene
) {
}
