package com.example.dzcom.interfaces.dto.response.risk;

import com.example.dzcom.application.dto.risk.RiskCheckView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 风险检查结果响应。 */
@Builder
@Schema(description = "风险检查结果响应")
public record RiskCheckResponse(
    @Schema(description = "风险检查业务唯一标识")
    String bizId,
    @Schema(description = "请求或业务链路追踪标识")
    String traceId,
    @Schema(description = "被检查业务类型")
    String businessType,
    @Schema(description = "被检查业务对象业务标识")
    String businessBizId,
    @Schema(description = "关联用户业务标识")
    String userBizId,
    @Schema(description = "规则编码快照")
    String ruleCode,
    @Schema(description = "规则版本快照")
    int ruleVersion,
    @Schema(description = "检查结论")
    String checkResult,
    @Schema(description = "风险等级")
    String riskLevel,
    @Schema(description = "风险评分")
    BigDecimal score,
    @Schema(description = "原因编码")
    String reasonCode,
    @Schema(description = "检查详情 JSON")
    String detail,
    @Schema(description = "检查时间")
    LocalDateTime checkedAt
) {
    /** 从应用层视图转换为接口响应。 */
    public static RiskCheckResponse from(RiskCheckView view) {
        return RiskCheckResponse.builder()
            .bizId(view.bizId())
            .traceId(view.traceId())
            .businessType(view.businessType())
            .businessBizId(view.businessBizId())
            .userBizId(view.userBizId())
            .ruleCode(view.ruleCode())
            .ruleVersion(view.ruleVersion())
            .checkResult(view.checkResult())
            .riskLevel(view.riskLevel())
            .score(view.score())
            .reasonCode(view.reasonCode())
            .detail(view.detail())
            .checkedAt(view.checkedAt())
            .build();
    }
}
