package com.example.dzcom.domain.model.account;

import com.example.dzcom.domain.enums.account.KycStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/** 用户当前 KYC 与投资风险承受能力画像。 */
@Builder(toBuilder = true)
public record UserRiskProfile(
    String bizId,
    String userBizId,
    KycStatus kycStatus,
    int riskLevel,
    String assessmentVersion,
    LocalDateTime assessedAt,
    LocalDateTime kycReviewedAt,
    int deleted
) {
    /**
     * 更新 KYC 状态并记录审核时间。
     *
     * @param target 目标状态或目标值
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public UserRiskProfile changeKycStatus(KycStatus target, LocalDateTime now) {
        return toBuilder().kycStatus(target).kycReviewedAt(now).build();
    }

    /**
     * 校验并更新风险等级，同时记录最近测评时间。
     *
     * @param target 目标状态或目标值
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    public UserRiskProfile changeRiskLevel(int target, LocalDateTime now) {
        if (target < 1 || target > 5) {
            throw new IllegalArgumentException("风险等级必须在 1 到 5 之间");
        }
        return toBuilder().riskLevel(target).assessedAt(now).build();
    }
}
