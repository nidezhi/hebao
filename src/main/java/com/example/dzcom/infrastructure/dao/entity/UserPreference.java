package com.example.dzcom.infrastructure.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好实体（对应数据库表 aiw_user_preference）
 */
@Data
@TableName("aiw_user_preference")
public class UserPreference {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String bizId;
    
    private String preferenceKey;
    
    private String preferenceValue;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer isDeleted;
}
