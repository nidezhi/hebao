package com.example.dzcom.infrastructure.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
@TableName("aiw_user")
public class UserEntity {

    /**
     * 业务主键ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String bizId;

    /**
     * 用户编号（唯一业务编号）
     */
    private String userNo;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 密码哈希值（BCrypt加密）
     */
    private String passwordHash;

    /**
     * KYC认证状态
     * 0: 未认证, 1: 已认证, 2: 审核中
     */
    private Integer kycStatus;

    /**
     * 风险等级（1-5级，1为最低风险）
     */
    private Integer riskLevel;

    /**
     * 用户状态
     * 0: 禁用, 1: 启用
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
     * 创建人ID
     */
    private String createdBy;

    /**
     * 更新人ID
     */
    private String updatedBy;

    /**
     * 逻辑删除标识
     * 0: 未删除, 1: 已删除
     */
    private Integer isDeleted;
}
