package com.example.dzcom.application.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/** AI Prompt 预览应用层视图。 */
@Builder
@Schema(description = "AI Prompt 预览应用层视图")
public record AiPromptPreviewView(
    @Schema(description = "Prompt模板业务唯一标识")
    String promptBizId,
    @Schema(description = "Prompt跨版本稳定编码")
    String promptCode,
    @Schema(description = "Prompt版本号")
    String promptVersion,
    @Schema(description = "使用场景")
    String scenario,
    @Schema(description = "渲染后的最终Prompt")
    String renderedPrompt,
    @Schema(description = "缺失变量名集合")
    List<String> missingVariables,
    @Schema(description = "预览是否可用于后续模型调用")
    boolean readyForModel,
    @Schema(description = "预览提示信息")
    String displayMessage
) {
}
