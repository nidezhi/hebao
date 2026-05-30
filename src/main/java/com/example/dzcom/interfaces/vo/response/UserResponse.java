package com.example.dzcom.interfaces.vo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应
 */
@Data
public class UserResponse {
    
    /**
     * 用户业务ID
     */
    private String bizId;
    
    /**
     * 用户编号
     */
    private String userNo;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * KYC状态 (0:未认证, 1:已认证, 2:审核中)
     */
    private Integer kycStatus;
    
    /**
     * 风险承受能力等级 (1-5)
     */
    private Integer riskLevel;
    
    /**
     * 账户状态 (0:禁用, 1:正常)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
