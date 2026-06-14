package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户认证凭据持久化实体。
 *
 * <p>密码只以不可逆哈希保存，并通过凭据版本控制历史会话失效。
 * 登录失败次数和临时锁定时间也在本实体中维护。</p>
 */
@TableName("aiw_user_credential")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredentialEntity {
    /** 凭据业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;

    /** 所属用户业务标识，仅为逻辑关联。 */
    private String userBizId;

    /** 凭据类型，首版固定为 PASSWORD。 */
    private String credentialType;

    /** BCrypt 等算法生成的密文，严禁输出到日志或响应。 */
    private String secretHash;

    /** 当前使用的哈希算法标识。 */
    private String hashAlgorithm;

    /** 改密后递增，用于使旧会话失效。 */
    private int credentialVersion;

    /** 凭据过期时间，为空表示不强制过期。 */
    private LocalDateTime expiresAt;

    /** 最近一次修改凭据的时间。 */
    private LocalDateTime changedAt;

    /** 连续登录失败次数，成功登录后归零。 */
    private int failedAttempts;

    /** 临时锁定截止时间。 */
    private LocalDateTime lockedUntil;

    /** 记录创建时间。 */
    private LocalDateTime createdAt;

    /** 记录最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记。 */
    @TableField("is_deleted")
    private int deleted;
}
