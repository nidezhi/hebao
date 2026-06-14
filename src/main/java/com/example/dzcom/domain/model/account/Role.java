package com.example.dzcom.domain.model.account;

import lombok.Builder;

import java.time.LocalDateTime;

/** 角色定义领域对象，承载角色状态和审计信息。 */
@Builder(toBuilder = true)
public record Role(
    String bizId,
    String roleCode,
    String roleName,
    String description,
    String roleType,
    int status,
    int version,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy,
    int deleted,
    LocalDateTime deletedAt
) {
    /** 判断角色是否处于可授权状态。 */
    public boolean active() {
        return status == 1 && deleted == 0;
    }

    /** 判断角色是否为系统内置角色。 */
    public boolean systemRole() {
        return "SYSTEM".equals(roleType);
    }
}
