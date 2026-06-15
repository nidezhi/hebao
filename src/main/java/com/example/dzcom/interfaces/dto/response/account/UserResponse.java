package com.example.dzcom.interfaces.dto.response.account;

import com.example.dzcom.application.dto.account.UserView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** 接口层用户响应，不直接暴露应用层或领域层对象。 */
@Builder
@Schema(description = "用户响应，包含登录标识、账户状态、角色和权限")
public record UserResponse(
    @Schema(description = "业务唯一标识", example = "usr_01Hxxxxx") String bizId,
    @Schema(description = "平台内部用户编号", example = "U202600001") String userNo,
    @Schema(description = "用户名", example = "alice") String username,
    @Schema(description = "邮箱", example = "alice@example.com") String email,
    @Schema(description = "手机号", example = "+8613800000000") String phone,
    @Schema(description = "展示昵称", example = "小张") String nickname,
    @Schema(description = "头像 URL") String avatarUrl,
    @Schema(description = "账户状态", example = "ACTIVE") String status,
    @Schema(description = "KYC 状态", example = "VERIFIED") String kycStatus,
    @Schema(description = "风险承受等级", example = "3") int riskLevel,
    @Schema(description = "有效角色编码集合") Set<String> roles,
    @Schema(description = "有效权限编码集合") Set<String> permissions,
    @Schema(description = "注册时间") LocalDateTime registeredAt,
    @Schema(description = "最近登录时间") LocalDateTime lastLoginAt
) {

    /**
     * 将应用层用户视图转换为接口响应。
     *
     * @param source 应用层用户视图
     * @return 接口层用户响应
     * @author dz
     * @date 2026-06-15
     */
    public static UserResponse from(UserView source) {
        return UserResponse.builder()
            .bizId(source.bizId())
            .userNo(source.userNo())
            .username(source.username())
            .email(source.email())
            .phone(source.phone())
            .nickname(source.nickname())
            .avatarUrl(source.avatarUrl())
            .status(source.status() == null ? null : source.status().name())
            .kycStatus(source.kycStatus() == null ? null : source.kycStatus().name())
            .riskLevel(source.riskLevel())
            .roles(source.roles())
            .permissions(source.permissions())
            .registeredAt(source.registeredAt())
            .lastLoginAt(source.lastLoginAt())
            .build();
    }
}
