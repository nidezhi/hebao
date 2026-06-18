package com.example.dzcom.domain.model.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户主体领域模型。
 *
 * <p>只负责账户生命周期行为，不混入登录标识、密码、资料和风险画像。
 * 状态只能通过领域方法变更，以保证禁用、锁定和删除规则集中维护。</p>
 */
@Schema(description = "用户账户主体领域模型")
@Getter
@Builder(toBuilder = true)
public class User {
    @Schema(description = "业务唯一标识")
    private final String bizId;
    @Schema(description = "平台用户编号（不可变）")
    private final String userNo;
    @Schema(description = "账户状态")
    private AccountStatus status;
    @Schema(description = "乐观锁版本")
    private int version;
    @Schema(description = "注册时间（北京时间）")
    private final LocalDateTime registeredAt;
    @Schema(description = "最近登录时间（北京时间）")
    private LocalDateTime lastLoginAt;
    @Schema(description = "记录创建时间（北京时间）")
    private final LocalDateTime createdAt;
    @Schema(description = "记录最后更新时间（北京时间）")
    private LocalDateTime updatedAt;
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
    @Schema(description = "删除时间（北京时间）")
    private LocalDateTime deletedAt;

    /**
     * 创建并初始化 User 对象。
     *
     * @param bizId 业务对象的唯一标识
     * @param userNo userNo 参数
     * @param status 目标状态或目标值
     * @param version version 参数
     * @param registeredAt registeredAt 参数
     * @param lastLoginAt lastLoginAt 参数
     * @param createdAt createdAt 参数
     * @param updatedAt updatedAt 参数
     * @param deleted deleted 参数
     * @param deletedAt deletedAt 参数
     * @author dz
     * @date 2026-06-14
     */
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

    /**
     * 创建处于正常状态的新用户主体。
     *
     * @param bizId 业务对象的唯一标识
     * @param userNo userNo 参数
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
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

    /**
     * 校验当前账户是否允许认证，删除、禁用和锁定状态均不可登录。
     *
     * @throws IllegalStateException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    public void ensureCanLogin() {
        if (!canLogin()) {
            throw new IllegalStateException("账户不可登录");
        }
    }

    /**
     * 返回当前账户是否满足登录所需的生命周期状态。
     *
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public boolean canLogin() {
        return deleted == 0 && status == AccountStatus.ACTIVE;
    }

    /**
     * 返回用户是否已被逻辑删除。 领域调用方使用布尔语义判断生命周期，避免在业务代码中传播数据库使用的 {@code 0/1} 存储约定。
     *
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public boolean isDeleted() {
        return deleted == 1;
    }

    /**
     * 由管理或安全用例变更账户状态。
     *
     * @param target 目标状态或目标值
     * @param now 当前业务时间
     * @throws IllegalStateException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    public void changeStatus(AccountStatus target, LocalDateTime now) {
        if (deleted == 1) {
            throw new IllegalStateException("用户不存在");
        }
        status = target;
        updatedAt = now;
    }

    /**
     * 登录成功后更新最后登录时间。
     *
     * @param now 当前业务时间
     * @author dz
     * @date 2026-06-14
     */
    public void recordSuccessfulLogin(LocalDateTime now) {
        lastLoginAt = now;
        updatedAt = now;
    }

    /**
     * 幂等执行逻辑删除，同时将账户置为禁用状态。
     *
     * @param now 当前业务时间
     * @author dz
     * @date 2026-06-14
     */
    public void delete(LocalDateTime now) {
        if (deleted == 0) {
            deleted = 1;
            deletedAt = now;
            status = AccountStatus.DISABLED;
            updatedAt = now;
        }
    }

}
