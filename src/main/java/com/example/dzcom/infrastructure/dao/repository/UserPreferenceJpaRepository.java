package com.example.dzcom.infrastructure.dao.repository;

import com.example.dzcom.infrastructure.dao.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户偏好JPA仓储接口（Infrastructure层）
 * <p>
 * 继承 JpaRepository，提供基础的 CRUD 操作
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Repository
public interface UserPreferenceJpaRepository extends JpaRepository<UserPreferenceEntity, Long> {

    /**
     * 根据业务用户ID和偏好键查询
     *
     * @param bizId 业务用户ID
     * @param preferenceKey 偏好配置键
     * @param isDeleted 删除标识
     * @return 用户偏好实体（可选）
     */
    Optional<UserPreferenceEntity> findByBizIdAndPreferenceKeyAndIsDeleted(
        String bizId, String preferenceKey, Integer isDeleted);

    /**
     * 根据业务用户ID查询所有偏好
     *
     * @param bizId 业务用户ID
     * @param isDeleted 删除标识
     * @return 用户偏好实体列表
     */
    List<UserPreferenceEntity> findByBizIdAndIsDeleted(String bizId, Integer isDeleted);

    /**
     * 根据业务用户ID删除所有偏好（逻辑删除）
     *
     * @param bizId 业务用户ID
     */
    void deleteByBizId(String bizId);
}
