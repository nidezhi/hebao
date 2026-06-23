package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/** AI Prompt 预览请求。 */
@Schema(description = "AI Prompt 预览请求")
public record AiPromptPreviewRequest(
    @Schema(description = "Prompt模板业务唯一标识，优先使用")
    String promptBizId,
    @Schema(description = "Prompt稳定编码，未传业务ID时使用")
    String promptCode,
    @Schema(description = "Prompt版本，未传业务ID时使用")
    String promptVersion,
    @Schema(description = "预览变量值，键为变量名，值为替换文本")
    Map<String, String> variables
) {
}
