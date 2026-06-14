package com.example.dzcom.application.dto.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** 聚合用户主体、标识、资料、风险画像和角色后的账户视图。 */
@Builder
public record UserView(
    String bizId,
    String userNo,
    String username,
    String email,
    String phone,
    String nickname,
    String avatarUrl,
    AccountStatus status,
    KycStatus kycStatus,
    int riskLevel,
    Set<String> roles,
    Set<String> permissions,
    LocalDateTime registeredAt,
    LocalDateTime lastLoginAt
) {
}
