package com.example.dzcom.context;

import lombok.Builder;
import lombok.Data;

/**
 * 用户上下文（ThreadLocal）
 * 用于在请求处理过程中存储和获取当前用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();
    
    @Data
    @Builder
    public static class UserInfo {
        private String userId;
        private String username;
        private String userNo;
    }
    
    /**
     * 设置当前用户信息
     */
    public static void setCurrentUser(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }
    
    /**
     * 获取当前用户信息
     */
    public static UserInfo getCurrentUser() {
        return USER_THREAD_LOCAL.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }
    
    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}
