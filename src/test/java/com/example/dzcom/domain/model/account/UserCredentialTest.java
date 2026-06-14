package com.example.dzcom.domain.model.account;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** 密码凭据失败计数、锁定和版本递增规则测试。 */
class UserCredentialTest {

    /**
     * 执行 change secret should increment version and clear lock 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void changeSecretShouldIncrementVersionAndClearLock() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 0, 0);
        UserCredential credential = UserCredential.builder()
            .bizId("credential-biz-id")
            .userBizId("user-biz-id")
            .secretHash("old-hash")
            .hashAlgorithm("BCRYPT")
            .credentialVersion(1)
            .failedAttempts(4)
            .lockedUntil(now.plusMinutes(15))
            .changedAt(now.minusDays(1))
            .build();

        UserCredential changed = credential.changeSecret("new-hash", now);

        assertThat(changed.secretHash()).isEqualTo("new-hash");
        assertThat(changed.credentialVersion()).isEqualTo(2);
        assertThat(changed.failedAttempts()).isZero();
        assertThat(changed.lockedUntil()).isNull();
        assertThat(changed.changedAt()).isEqualTo(now);
    }
}
