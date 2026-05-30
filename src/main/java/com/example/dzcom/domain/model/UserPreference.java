package com.example.dzcom.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好值对象
 */
@Data
@Builder
public class UserPreference {
    
    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 用户业务ID
     */
    private String bizId;
    
    /**
     * 偏好键
     */
    private String preferenceKey;
    
    /**
     * 偏好值
     */
    private String preferenceValue;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 是否删除
     */
    private Integer isDeleted;
}
