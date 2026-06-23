package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiPromptPreviewView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/** AI Prompt 预览响应。 */
@Builder
@Schema(description = "AI Prompt 预览响应")
public record AiPromptPreviewResponse(
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
    /** 从应用层视图转换为接口响应。 */
    public static AiPromptPreviewResponse from(AiPromptPreviewView view) {
        return AiPromptPreviewResponse.builder()
            .promptBizId(view.promptBizId())
            .promptCode(view.promptCode())
            .promptVersion(view.promptVersion())
            .scenario(view.scenario())
            .renderedPrompt(view.renderedPrompt())
            .missingVariables(view.missingVariables())
            .readyForModel(view.readyForModel())
            .displayMessage(view.displayMessage())
            .build();
    }
}
