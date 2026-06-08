package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 用户角色分配持久化实体，只保存角色结果，不承载权限定义。 */
@Entity
@Table(name = "aiw_user_role")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRoleEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "user_biz_id", nullable = false, length = 36)
    private String userBizId;
    @Column(name = "role_code", nullable = false, length = 64)
    private String roleCode;
    @Column(name = "scope_code", nullable = false, length = 64)
    private String scopeCode;
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "created_by", length = 64)
    private String createdBy;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT")
    private int deleted;
}
