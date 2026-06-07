package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


/**
 * 用户主体持久化实体。
 *
 * <p>该表只承载稳定的用户身份、生命周期状态和审计时间，不保存用户名、
 * 邮箱、手机号、密码或风险画像。其他账户数据通过 {@code bizId} 进行逻辑关联，
 * 不配置 JPA 对象关系和数据库外键。</p>
 */
@Entity
@Table(name = "aiw_user")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEntity {
    /** 对外和跨模块使用的用户业务唯一标识。 */
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;

    /** 面向运营、客服和用户展示的稳定用户编号，创建后不可修改。 */
    @Column(name = "user_no", nullable = false, length = 32)
    private String userNo;

    /** 账户状态编码：0-禁用，1-正常，2-锁定。 */
    @Column(nullable = false)
    private int status;

    /** JPA 乐观锁版本，防止并发更新覆盖。 */
    @Version
    @Column(nullable = false)
    private int version;

    /** 用户完成注册的 UTC 时间。 */
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    /** 最近一次成功登录的 UTC 时间。 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 记录创建时间。 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 记录最后更新时间。 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 创建该用户的操作者业务标识或系统标识。 */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /** 最后修改该用户的操作者业务标识或系统标识。 */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /** 逻辑删除标记，删除后常规查询必须排除。 */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    /** 执行逻辑删除的 UTC 时间。 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
