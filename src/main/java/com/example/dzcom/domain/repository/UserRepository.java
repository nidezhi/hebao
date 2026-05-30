package com.example.dzcom.domain.repository;

import com.example.dzcom.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    
    /**
     * 根据ID查询用户
     */
    Optional<User> findById(String bizId);
    
    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 根据用户编号查询用户
     */
    Optional<User> findByUserNo(String userNo);
    
    /**
     * 保存用户
     */
    void save(User user);
    
    /**
     * 更新用户
     */
    void update(User user);
    
    /**
     * 删除用户（逻辑删除）
     */
    void deleteById(String bizId);
    
    /**
     * 查询所有用户（分页）
     */
    List<User> findAll(int pageNum, int pageSize);
    
    /**
     * 统计用户总数
     */
    long count();
}
