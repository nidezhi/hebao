package com.example.dzcom.application.service;

import com.example.dzcom.infrastructure.dao.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户认证（登录）
     */
    User authenticate(String username, String password);
    
    /**
     * 根据ID获取用户
     */
    User getById(String userId);
}
