package com.example.dzcom.infrastructure.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全工具类
 */
public class SecurityUtil {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     */
    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    private SecurityUtil() {
        // 防止实例化
    }
}
