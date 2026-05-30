package com.example.dzcom.common.constant;

/**
 * Redis键前缀常量
 */
public class RedisKeyConstant {
    
    /**
     * 认证Token前缀
     * 完整key: auth:token:{userId}
     */
    public static final String AUTH_TOKEN_PREFIX = "auth:token:";
    
    /**
     * 用户信息缓存前缀
     * 完整key: user:info:{userId}
     */
    public static final String USER_INFO_PREFIX = "user:info:";
    
    /**
     * 验证码前缀
     * 完整key: captcha:{sessionId}
     */
    public static final String CAPTCHA_PREFIX = "captcha:";
    
    /**
     * 登录失败次数前缀
     * 完整key: login:fail:{username}
     */
    public static final String LOGIN_FAIL_PREFIX = "login:fail:";
    
    private RedisKeyConstant() {
        // 防止实例化
    }
}
