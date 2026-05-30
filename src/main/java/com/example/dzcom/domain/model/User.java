package com.example.dzcom.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户聚合根（Domain层）
 * <p>
 * 用户领域的核心业务对象，封装用户相关的业务逻辑和规则
 * 包含用户的基本信息、认证状态、风险等级等核心属性
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Data
@Builder
public class User {
    
    /**
     * 用户业务ID（UUID格式，全局唯一标识）
     */
    private String bizId;
    
    /**
     * 用户编号（业务可读的编号，如：U20260530001）
     */
    private String userNo;
    
    /**
     * 用户名（登录账号，唯一）
     */
    private String username;
    
    /**
     * 邮箱地址（可选，用于接收通知）
     */
    private String email;
    
    /**
     * 手机号码（可选，用于短信验证）
     */
    private String phone;
    
    /**
     * 密码哈希值（BCrypt加密存储）
     */
    private String passwordHash;
    
    /**
     * KYC认证状态
     * <ul>
     *   <li>0: 未认证</li>
     *   <li>1: 已认证</li>
     *   <li>2: 审核中</li>
     * </ul>
     */
    private Integer kycStatus;
    
    /**
     * 风险承受能力等级（1-5级）
     * <ul>
     *   <li>1: 保守型</li>
     *   <li>2: 稳健型</li>
     *   <li>3: 平衡型</li>
     *   <li>4: 成长型</li>
     *   <li>5: 进取型</li>
     * </ul>
     */
    private Integer riskLevel;
    
    /**
     * 账户状态
     * <ul>
     *   <li>0: 禁用（无法登录）</li>
     *   <li>1: 正常</li>
     * </ul>
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
     * 逻辑删除标识
     * <ul>
     *   <li>0: 未删除</li>
     *   <li>1: 已删除</li>
     * </ul>
     */
    private Integer isDeleted;
    
    // ==================== 领域行为 ====================
    
    /**
     * 判断用户是否处于激活状态
     *
     * @return true-激活, false-禁用
     */
    public boolean isActive() {
        return this.status != null && this.status == 1;
    }
    
    /**
     * 判断用户是否已完成KYC认证
     *
     * @return true-已认证, false-未认证或审核中
     */
    public boolean isKycVerified() {
        return this.kycStatus != null && this.kycStatus == 1;
    }
    
    /**
     * 更新用户风险等级
     * <p>
     * 业务规则：风险等级必须在1-5之间
     * </p>
     *
     * @param riskLevel 新的风险等级（1-5）
     * @throws IllegalArgumentException 风险等级超出范围时抛出
     */
    public void updateRiskLevel(Integer riskLevel) {
        if (riskLevel == null || riskLevel < 1 || riskLevel > 5) {
            throw new IllegalArgumentException("风险等级必须在1-5之间");
        }
        this.riskLevel = riskLevel;
    }
    
    /**
     * 禁用用户账户
     * <p>
     * 将用户状态设置为禁用，用户将无法登录系统
     * </p>
     */
    public void disable() {
        this.status = 0;
    }
    
    /**
     * 启用用户账户
     * <p>
     * 将用户状态设置为正常，用户可以正常登录使用
     * </p>
     */
    public void enable() {
        this.status = 1;
    }
}
