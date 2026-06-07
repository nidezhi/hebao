package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户登录标识持久化实体，用于保存用户名、邮箱、手机号等可认证标识。
 *
 * <p>{@code normalizedValue} 用于登录查询和唯一性判断，
 * {@code identityValue} 只用于经过权限控制后的展示。</p>
 */
@Entity
@Table(name = "aiw_user_identity")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserIdentityEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "user_biz_id", nullable = false, length = 36)
    private String userBizId;
    @Column(name = "identity_type", nullable = false, length = 32)
    private String identityType;
    @Column(name = "identity_value", nullable = false, length = 256)
    private String identityValue;
    @Column(name = "normalized_value", nullable = false, length = 256)
    private String normalizedValue;
    @Column(nullable = false)
    private boolean verified;
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    @Column(nullable = false)
    private int status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
