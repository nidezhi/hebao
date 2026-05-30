package com.example.dzcom.infrastructure.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("biz_user")
public class User {
    
    @TableId(type = IdType.ASSIGN_UUID)
    private String bizId;
    
    private String username;
    
    private String password;
    
    private String userNo;
    
    private String email;
    
    private String phone;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
