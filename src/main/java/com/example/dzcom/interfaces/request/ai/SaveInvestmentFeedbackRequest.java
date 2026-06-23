package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存投资反馈请求。 */
@Schema(description = "保存投资反馈请求")
public record SaveInvestmentFeedbackRequest(
    @NotBlank
    @Schema(description = "反馈目标类型：REPORT/RECOMMENDATION/MOCK_ORDER/MOCK_PORTFOLIO/BACKTEST/PROMPT")
    String targetType,
    @NotBlank
    @Schema(description = "反馈目标业务标识")
    String targetBizId,
    @Schema(description = "关联投资报告业务标识")
    String reportBizId,
    @Schema(description = "关联Prompt模板业务标识")
    String promptBizId,
    @Schema(description = "Prompt稳定编码快照")
    String promptCode,
    @Schema(description = "Prompt版本快照")
    String promptVersion,
    @Schema(description = "关联回测结果业务标识")
    String backtestBizId,
    @NotBlank
    @Schema(description = "反馈动作：ADOPT/REJECT/WATCH/IGNORE")
    String feedbackAction,
    @Schema(description = "原因编码")
    String reasonCode,
    @Schema(description = "用户或人工复核备注")
    String commentText,
    @Schema(description = "反馈上下文 JSON")
    String metadata
) {
}
