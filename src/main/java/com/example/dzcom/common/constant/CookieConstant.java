package com.example.dzcom.common.constant;

/**
 * Cookie常量
 */
public class CookieConstant {
    
    /**
     * 认证Token的Cookie名称
     */
    public static final String AUTH_TOKEN_COOKIE_NAME = "AUTH_TOKEN";
    
    /**
     * Cookie默认过期时间（7天，单位：秒）
     */
    public static final int DEFAULT_MAX_AGE = 7 * 24 * 3600;
    
    private CookieConstant() {
        // 防止实例化
    }
}
