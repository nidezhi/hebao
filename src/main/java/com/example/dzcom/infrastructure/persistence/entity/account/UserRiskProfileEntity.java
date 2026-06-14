package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 用户 KYC 状态和投资风险承受能力的当前画像。 */
@TableName("aiw_user_risk_profile")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRiskProfileEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String userBizId;
    private int kycStatus;
    private int riskLevel;
    private String assessmentVersion;
    private LocalDateTime assessedAt;
    private LocalDateTime kycReviewedAt;
    private String extData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    private int deleted;
}
