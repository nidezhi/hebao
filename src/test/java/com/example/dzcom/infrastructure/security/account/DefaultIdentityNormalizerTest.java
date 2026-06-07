package com.example.dzcom.infrastructure.security.account;

import com.example.dzcom.domain.enums.account.IdentityType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 用户名、邮箱和手机号识别及标准化规则测试。 */
class DefaultIdentityNormalizerTest {
    private final DefaultIdentityNormalizer normalizer = new DefaultIdentityNormalizer();

    @Test
    void shouldDetectAndNormalizeEmail() {
        assertThat(normalizer.detectType(" User@Example.COM ")).isEqualTo(IdentityType.EMAIL);
        assertThat(normalizer.normalize(IdentityType.EMAIL, " User@Example.COM "))
            .isEqualTo("user@example.com");
    }

    @Test
    void shouldDetectAndNormalizePhone() {
        assertThat(normalizer.detectType("13800138000")).isEqualTo(IdentityType.PHONE);
        assertThat(normalizer.normalize(IdentityType.PHONE, "13800138000"))
            .isEqualTo("+13800138000");
    }
}
