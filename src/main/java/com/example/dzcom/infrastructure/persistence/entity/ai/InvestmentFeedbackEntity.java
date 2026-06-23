package com.example.dzcom.infrastructure.persistence.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 投资闭环反馈持久化实体。 */
@Schema(description = "投资闭环反馈持久化实体")
@TableName("aiw_investment_feedback")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentFeedbackEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "反馈业务唯一标识")
    private String bizId;
    @Schema(description = "反馈用户业务标识")
    private String userBizId;
    @Schema(description = "反馈目标类型")
    private String targetType;
    @Schema(description = "反馈目标业务标识")
    private String targetBizId;
    @Schema(description = "关联投资报告业务标识")
    private String reportBizId;
    @Schema(description = "关联Prompt模板业务标识")
    private String promptBizId;
    @Schema(description = "Prompt稳定编码快照")
    private String promptCode;
    @Schema(description = "Prompt版本快照")
    private String promptVersion;
    @Schema(description = "关联回测结果业务标识")
    private String backtestBizId;
    @Schema(description = "反馈动作")
    private String feedbackAction;
    @Schema(description = "原因编码")
    private String reasonCode;
    @Schema(description = "用户或人工复核备注")
    private String commentText;
    @Schema(description = "反馈上下文 JSON")
    private String metadata;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
