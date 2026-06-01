package com.example.dzcom.infrastructure.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Entity
@Table(name = "aiw_user_preference")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceEntity {

    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务用户ID（关联 aiw_user.biz_id）
     */
    @Column(name = "biz_id", length = 36)
    private String bizId;

    /**
     * 偏好配置键（如：theme, language, notification_enabled）
     */
    @Column(name = "preference_key", length = 100)
    private String preferenceKey;

    /**
     * 偏好配置值（JSON格式存储）
     */
    @Column(name = "preference_value", columnDefinition = "TEXT")
    private String preferenceValue;

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
     * 逻辑删除标识
     * 0: 未删除, 1: 已删除
     */
    @Column(name = "is_deleted", columnDefinition = "TINYINT")
    private Integer isDeleted;
}
