package com.example.dzcom.application.service;

import com.example.dzcom.domain.model.User;
import com.example.dzcom.interfaces.vo.request.CreateUserRequest;
import com.example.dzcom.interfaces.vo.request.UpdateUserRequest;
import com.example.dzcom.interfaces.vo.response.UserResponse;

import java.util.List;

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
    
    /**
     * 创建用户
     */
    String create(CreateUserRequest request);
    
    /**
     * 更新用户
     */
    void update(String bizId, UpdateUserRequest request);
    
    /**
     * 删除用户
     */
    void delete(String bizId);
    
    /**
     * 查询用户详情
     */
    UserResponse getUserDetail(String bizId);
    
    /**
     * 分页查询用户列表
     */
    List<UserResponse> listUsers(int pageNum, int pageSize);
    
    /**
     * 统计用户总数
     */
    long count();
    
    /**
     * 启用用户
     */
    void enableUser(String bizId);
    
    /**
     * 禁用用户
     */
    void disableUser(String bizId);
    
    /**
     * 更新风险等级
     */
    void updateRiskLevel(String bizId, Integer riskLevel);
}
