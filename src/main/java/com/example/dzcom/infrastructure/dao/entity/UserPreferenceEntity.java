package com.example.dzcom.infrastructure.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好数据库实体（Infrastructure层）
 * <p>
 * 对应数据库表：aiw_user_preference
 * 用于持久化用户偏好设置，与领域模型 UserPreference 区分
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Data
@TableName("aiw_user_preference")
public class UserPreferenceEntity {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务用户ID（关联 aiw_user.biz_id）
     */
    private String bizId;

    /**
     * 偏好配置键（如：theme, language, notification_enabled）
     */
    private String preferenceKey;

    /**
     * 偏好配置值（JSON格式存储）
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
     * 逻辑删除标识
     * 0: 未删除, 1: 已删除
     */
    private Integer isDeleted;
}
