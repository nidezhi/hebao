package com.example.dzcom.infrastructure.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（对应数据库表 aiw_user）
 */
@Data
@TableName("aiw_user")
public class User {
    
    @TableId(type = IdType.ASSIGN_UUID)
    private String bizId;
    
    private String userNo;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String passwordHash;
    
    private Integer kycStatus;
    
    private Integer riskLevel;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    private Integer isDeleted;
}
