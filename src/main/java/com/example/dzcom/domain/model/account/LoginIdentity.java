package com.example.dzcom.domain.model.account;

import com.example.dzcom.domain.enums.account.IdentityType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 登录标识领域对象，封装标识类型、原始展示值和用于唯一查询的标准化值。
 */
@Builder(toBuilder = true)
public record LoginIdentity(
    String bizId,
    String userBizId,
    IdentityType type,
    String value,
    String normalizedValue,
    boolean verified,
    boolean active,
    LocalDateTime createdAt,
    boolean deleted
) {
}
