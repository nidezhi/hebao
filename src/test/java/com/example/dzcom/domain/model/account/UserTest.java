package com.example.dzcom.domain.model.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 用户主体领域规则测试。
 *
 * <p>验证登录资格、状态变更和逻辑删除均通过领域行为完成，
 * 避免应用服务绕过账户生命周期约束。</p>
 */
class UserTest {

    /**
     * 执行 registered user should be active and can login 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void registeredUserShouldBeActiveAndCanLogin() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 0, 0);

        User user = User.register("user-biz-id", "U202606080001", now);

        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.isDeleted()).isFalse();
        user.ensureCanLogin();
    }

    /**
     * 执行 disabled user should not login 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void disabledUserShouldNotLogin() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 0, 0);
        User user = User.register("user-biz-id", "U202606080001", now);
        user.changeStatus(AccountStatus.DISABLED, now.plusMinutes(1));

        assertThatThrownBy(user::ensureCanLogin)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("账户不可登录");
    }

    /**
     * 执行 delete should be idempotent and disable account 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void deleteShouldBeIdempotentAndDisableAccount() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 0, 0);
        User user = User.register("user-biz-id", "U202606080001", now);
        LocalDateTime deletedAt = now.plusMinutes(1);

        user.delete(deletedAt);
        user.delete(deletedAt.plusMinutes(1));

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getStatus()).isEqualTo(AccountStatus.DISABLED);
        assertThat(user.getDeletedAt()).isEqualTo(deletedAt);
    }
}
