package com.example.dzcom.infrastructure.config.account;

import com.example.dzcom.domain.service.account.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 计算输入内容的安全哈希值。
     *
     * @param rawPassword rawPassword 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 校验原始值与哈希值是否匹配。
     *
     * @param rawPassword rawPassword 参数
     * @param passwordHash passwordHash 参数
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}
