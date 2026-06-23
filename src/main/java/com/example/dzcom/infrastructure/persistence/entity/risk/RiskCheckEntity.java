package com.example.dzcom.infrastructure.persistence.entity.risk;

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

/** 风险检查结果持久化实体。 */
@Schema(description = "风险检查结果持久化实体")
@TableName("aiw_risk_check")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RiskCheckEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "风险检查业务唯一标识")
    private String bizId;
    @Schema(description = "请求或业务链路追踪标识")
    private String traceId;
    @Schema(description = "被检查业务类型")
    private String businessType;
    @Schema(description = "被检查业务对象业务标识")
    private String businessBizId;
    @Schema(description = "关联用户业务标识")
    private String userBizId;
    @Schema(description = "规则编码快照")
    private String ruleCode;
    @Schema(description = "规则版本快照")
    private int ruleVersion;
    @Schema(description = "检查结论")
    private String checkResult;
    @Schema(description = "风险等级")
    private String riskLevel;
    @Schema(description = "风险评分")
    private BigDecimal score;
    @Schema(description = "原因编码")
    private String reasonCode;
    @Schema(description = "检查详情 JSON")
    private String detail;
    @Schema(description = "检查时间")
    private LocalDateTime checkedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
