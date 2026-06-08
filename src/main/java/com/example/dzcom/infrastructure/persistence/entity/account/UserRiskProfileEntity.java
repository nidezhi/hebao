package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 用户 KYC 状态和投资风险承受能力的当前画像。 */
@Entity
@Table(name = "aiw_user_risk_profile")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRiskProfileEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "user_biz_id", nullable = false, length = 36)
    private String userBizId;
    @Column(name = "kyc_status", nullable = false)
    private int kycStatus;
    @Column(name = "risk_level", nullable = false)
    private int riskLevel;
    @Column(name = "assessment_version", length = 32)
    private String assessmentVersion;
    @Column(name = "assessed_at")
    private LocalDateTime assessedAt;
    @Column(name = "kyc_reviewed_at")
    private LocalDateTime kycReviewedAt;
    @Column(name = "ext_data", columnDefinition = "json")
    private String extData;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private int deleted;
}
