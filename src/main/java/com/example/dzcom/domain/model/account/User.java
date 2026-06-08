package com.example.dzcom.domain.model.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户主体领域模型。
 *
 * <p>只负责账户生命周期行为，不混入登录标识、密码、资料和风险画像。
 * 状态只能通过领域方法变更，以保证禁用、锁定和删除规则集中维护。</p>
 */
@Getter
@Builder(toBuilder = true)
public class User {
    private final String bizId;
    private final String userNo;
    private AccountStatus status;
    private int version;
    private final LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deleted;
    private LocalDateTime deletedAt;

    private User(String bizId, String userNo, AccountStatus status, int version,
                 LocalDateTime registeredAt, LocalDateTime lastLoginAt,
                 LocalDateTime createdAt, LocalDateTime updatedAt,
                 int deleted, LocalDateTime deletedAt) {
        this.bizId = bizId;
        this.userNo = userNo;
        this.status = status;
        this.version = version;
        this.registeredAt = registeredAt;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    /** 创建处于正常状态的新用户主体。 */
    public static User register(String bizId, String userNo, LocalDateTime now) {
        return User.builder()
            .bizId(bizId)
            .userNo(userNo)
            .status(AccountStatus.ACTIVE)
            .version(0)
            .registeredAt(now)
            .createdAt(now)
            .updatedAt(now)
            .deleted(0)
            .build();
    }

    /** 校验当前账户是否允许认证，删除、禁用和锁定状态均不可登录。 */
    public void ensureCanLogin() {
        if (!canLogin()) {
            throw new IllegalStateException("账户不可登录");
        }
    }

    /** 返回当前账户是否满足登录所需的生命周期状态。 */
    public boolean canLogin() {
        return deleted == 0 && status == AccountStatus.ACTIVE;
    }

    /**
     * 返回用户是否已被逻辑删除。
     *
     * <p>领域调用方使用布尔语义判断生命周期，避免在业务代码中传播数据库使用的
     * {@code 0/1} 存储约定。</p>
     */
    public boolean isDeleted() {
        return deleted == 1;
    }

    /** 由管理或安全用例变更账户状态。 */
    public void changeStatus(AccountStatus target, LocalDateTime now) {
        if (deleted == 1) {
            throw new IllegalStateException("用户不存在");
        }
        status = target;
        updatedAt = now;
    }

    /** 登录成功后更新最后登录时间。 */
    public void recordSuccessfulLogin(LocalDateTime now) {
        lastLoginAt = now;
        updatedAt = now;
    }

    /** 幂等执行逻辑删除，同时将账户置为禁用状态。 */
    public void delete(LocalDateTime now) {
        if (deleted == 0) {
            deleted = 1;
            deletedAt = now;
            status = AccountStatus.DISABLED;
            updatedAt = now;
        }
    }

}
