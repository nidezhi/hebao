package com.example.dzcom.infrastructure.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户数据库实体（Infrastructure层）
 * <p>
 * 对应数据库表：aiw_user
 * 用于持久化用户信息到数据库，与领域模型 User 区分
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Data
@Entity
@Table(name = "aiw_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * 业务主键ID（UUID格式）
     */
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;

    /**
     * 用户编号（唯一业务编号）
     */
    @Column(name = "user_no", length = 50)
    private String userNo;

    /**
     * 用户名（登录账号）
     */
    @Column(name = "username", length = 100, unique = true)
    private String username;

    /**
     * 邮箱地址
     */
    @Column(name = "email", length = 200)
    private String email;

    /**
     * 手机号码
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 密码哈希值（BCrypt加密）
     */
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    /**
     * KYC认证状态
     * 0: 未认证, 1: 已认证, 2: 审核中
     */
    @Column(name = "kyc_status")
    private Integer kycStatus;

    /**
     * 风险等级（1-5级，1为最低风险）
     */
    @Column(name = "risk_level")
    private Integer riskLevel;

    /**
     * 用户状态
     * 0: 禁用, 1: 启用
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @Column(name = "created_by", length = 36)
    private String createdBy;

    /**
     * 更新人ID
     */
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    /**
     * 逻辑删除标识
     * 0: 未删除, 1: 已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted;
}
