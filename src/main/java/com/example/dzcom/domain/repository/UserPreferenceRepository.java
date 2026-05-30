package com.example.dzcom.domain.repository;

import com.example.dzcom.domain.model.UserPreference;

import java.util.List;
import java.util.Optional;

/**
 * 用户偏好仓储接口
 */
public interface UserPreferenceRepository {
    
    /**
     * 根据ID查询
     */
    Optional<UserPreference> findById(Long id);
    
    /**
     * 根据用户ID和偏好键查询
     */
    Optional<UserPreference> findByBizIdAndKey(String bizId, String preferenceKey);
    
    /**
     * 查询用户的所有偏好
     */
    List<UserPreference> findByBizId(String bizId);
    
    /**
     * 保存偏好
     */
    void save(UserPreference preference);
    
    /**
     * 更新偏好
     */
    void update(UserPreference preference);
    
    /**
     * 删除偏好
     */
    void deleteById(Long id);
    
    /**
     * 批量删除用户的所有偏好
     */
    void deleteByBizId(String bizId);
}
