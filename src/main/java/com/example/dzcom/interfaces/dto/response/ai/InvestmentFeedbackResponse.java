package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.InvestmentFeedbackView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资反馈响应。 */
@Builder
@Schema(description = "投资反馈响应")
public record InvestmentFeedbackResponse(
    @Schema(description = "反馈业务唯一标识") String bizId,
    @Schema(description = "反馈用户业务标识") String userBizId,
    @Schema(description = "反馈目标类型") String targetType,
    @Schema(description = "反馈目标业务标识") String targetBizId,
    @Schema(description = "关联投资报告业务标识") String reportBizId,
    @Schema(description = "关联Prompt模板业务标识") String promptBizId,
    @Schema(description = "Prompt稳定编码快照") String promptCode,
    @Schema(description = "Prompt版本快照") String promptVersion,
    @Schema(description = "关联回测结果业务标识") String backtestBizId,
    @Schema(description = "反馈动作") String feedbackAction,
    @Schema(description = "原因编码") String reasonCode,
    @Schema(description = "用户或人工复核备注") String commentText,
    @Schema(description = "反馈上下文 JSON") String metadata,
    @Schema(description = "创建时间") LocalDateTime createdAt
) {
    /** 从应用层视图转换为接口响应。 */
    public static InvestmentFeedbackResponse from(InvestmentFeedbackView view) {
        return InvestmentFeedbackResponse.builder()
            .bizId(view.bizId())
            .userBizId(view.userBizId())
            .targetType(view.targetType())
            .targetBizId(view.targetBizId())
            .reportBizId(view.reportBizId())
            .promptBizId(view.promptBizId())
            .promptCode(view.promptCode())
            .promptVersion(view.promptVersion())
            .backtestBizId(view.backtestBizId())
            .feedbackAction(view.feedbackAction())
            .reasonCode(view.reasonCode())
            .commentText(view.commentText())
            .metadata(view.metadata())
            .createdAt(view.createdAt())
            .build();
    }
}
