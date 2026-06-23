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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** AI Prompt 评估持久化实体。 */
@Schema(description = "AI Prompt 评估持久化实体")
@TableName("aiw_ai_prompt_evaluation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiPromptEvaluationEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "Prompt评估业务唯一标识")
    private String bizId;
    @Schema(description = "Prompt模板业务标识")
    private String promptBizId;
    @Schema(description = "Prompt稳定编码快照")
    private String promptCode;
    @Schema(description = "Prompt版本快照")
    private String promptVersion;
    @Schema(description = "使用场景快照")
    private String scenario;
    @Schema(description = "关联回测结果业务标识")
    private String backtestBizId;
    @Schema(description = "关联反馈业务标识")
    private String feedbackBizId;
    @Schema(description = "综合评分")
    private BigDecimal score;
    @Schema(description = "评分详情 JSON")
    private String scoreDetail;
    @Schema(description = "复核状态")
    private String reviewStatus;
    @Schema(description = "评估来源")
    private String evaluatorType;
    @Schema(description = "评估人或任务业务标识")
    private String evaluatorBizId;
    @Schema(description = "评估时间")
    private LocalDateTime evaluatedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
