package com.example.dzcom.domain.model.account;

import lombok.Builder;

import java.time.LocalDateTime;

/** 用户角色分配领域对象。 */
@Builder(toBuilder = true)
public record UserRole(
    String bizId,
    String userBizId,
    String roleCode,
    String scopeCode,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveTo,
    int deleted
) {
}
