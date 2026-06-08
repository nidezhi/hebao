package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户认证凭据持久化实体。
 *
 * <p>密码只以不可逆哈希保存，并通过凭据版本控制历史会话失效。
 * 登录失败次数和临时锁定时间也在本实体中维护。</p>
 */
@Entity
@Table(name = "aiw_user_credential")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredentialEntity {
    /** 凭据业务唯一标识。 */
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;

    /** 所属用户业务标识，仅为逻辑关联。 */
    @Column(name = "user_biz_id", nullable = false, length = 36)
    private String userBizId;

    /** 凭据类型，首版固定为 PASSWORD。 */
    @Column(name = "credential_type", nullable = false, length = 32)
    private String credentialType;

    /** BCrypt 等算法生成的密文，严禁输出到日志或响应。 */
    @Column(name = "secret_hash", nullable = false, length = 512)
    private String secretHash;

    /** 当前使用的哈希算法标识。 */
    @Column(name = "hash_algorithm", nullable = false, length = 32)
    private String hashAlgorithm;

    /** 改密后递增，用于使旧会话失效。 */
    @Column(name = "credential_version", nullable = false)
    private int credentialVersion;

    /** 凭据过期时间，为空表示不强制过期。 */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** 最近一次修改凭据的时间。 */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /** 连续登录失败次数，成功登录后归零。 */
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    /** 临时锁定截止时间。 */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /** 记录创建时间。 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 记录最后更新时间。 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记。 */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT")
    private int deleted;
}
