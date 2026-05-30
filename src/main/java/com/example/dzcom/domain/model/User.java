package com.example.dzcom.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户聚合根
 */
@Data
@Builder
public class User {
    
    /**
     * 用户业务ID (UUID)
     */
    private String bizId;
    
    /**
     * 用户编号 (业务可读)
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
     * 密码哈希
     */
    private String passwordHash;
    
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
    
    /**
     * 是否删除 (0:否, 1:是)
     */
    private Integer isDeleted;
    
    // ==================== 领域行为 ====================
    
    /**
     * 验证用户是否激活
     */
    public boolean isActive() {
        return this.status != null && this.status == 1;
    }
    
    /**
     * 验证用户是否已完成KYC
     */
    public boolean isKycVerified() {
        return this.kycStatus != null && this.kycStatus == 1;
    }
    
    /**
     * 更新风险等级
     */
    public void updateRiskLevel(Integer riskLevel) {
        if (riskLevel == null || riskLevel < 1 || riskLevel > 5) {
            throw new IllegalArgumentException("风险等级必须在1-5之间");
        }
        this.riskLevel = riskLevel;
    }
    
    /**
     * 禁用账户
     */
    public void disable() {
        this.status = 0;
    }
    
    /**
     * 启用账户
     */
    public void enable() {
        this.status = 1;
    }
}
