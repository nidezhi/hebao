package com.example.dzcom.application.dto.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** 聚合用户主体、标识、资料、风险画像和角色后的账户视图。 */
@Builder
@Schema(description = "聚合用户账户信息的应用层视图")
public record UserView(
    @Schema(description = "用户业务唯一标识")
    String bizId,
    @Schema(description = "平台用户编号")
    String userNo,
    @Schema(description = "用户名登录标识")
    String username,
    @Schema(description = "邮箱登录标识")
    String email,
    @Schema(description = "手机号登录标识")
    String phone,
    @Schema(description = "用户展示昵称")
    String nickname,
    @Schema(description = "用户头像地址")
    String avatarUrl,
    @Schema(description = "账户状态")
    AccountStatus status,
    @Schema(description = "KYC 状态")
    KycStatus kycStatus,
    @Schema(description = "风险承受等级，范围 1-5")
    int riskLevel,
    @Schema(description = "用户当前有效角色编码集合")
    Set<String> roles,
    @Schema(description = "用户当前有效权限编码集合")
    Set<String> permissions,
    @Schema(description = "用户注册时间，北京时间")
    LocalDateTime registeredAt,
    @Schema(description = "最近成功登录时间，北京时间")
    LocalDateTime lastLoginAt
) {
}
