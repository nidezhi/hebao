package com.example.dzcom.infrastructure.dao.repository;

import cn.hutool.core.bean.BeanUtil;
import com.example.dzcom.domain.model.UserPreference;
import com.example.dzcom.domain.repository.UserPreferenceRepository;
import com.example.dzcom.infrastructure.dao.entity.UserPreferenceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户偏好仓储实现（Infrastructure层）
 * <p>
 * 实现领域层定义的 UserPreferenceRepository 接口，负责用户偏好数据的持久化操作
 * 使用 Spring Data JPA 进行数据库访问，完成实体与领域模型的转换
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Repository
@RequiredArgsConstructor
public class UserPreferenceRepositoryImpl implements UserPreferenceRepository {

    /**
     * 用户偏好JPA仓储接口
     */
    private final UserPreferenceJpaRepository userPreferenceJpaRepository;

    /**
     * 根据主键ID查询用户偏好
     *
     * @param id 主键ID
     * @return 用户偏好领域模型（可选）
     */
    @Override
    public Optional<UserPreference> findById(Long id) {
        return userPreferenceJpaRepository.findById(id)
            .map(this::convertToDomain);
    }

    /**
     * 根据业务用户ID和偏好键查询
     *
     * @param bizId 业务用户ID
     * @param preferenceKey 偏好配置键
     * @return 用户偏好领域模型（可选）
     */
    @Override
    public Optional<UserPreference> findByBizIdAndKey(String bizId, String preferenceKey) {
        return userPreferenceJpaRepository.findByBizIdAndPreferenceKeyAndIsDeleted(bizId, preferenceKey, 0)
            .map(this::convertToDomain);
    }

    /**
     * 根据业务用户ID查询所有偏好
     *
     * @param bizId 业务用户ID
     * @return 用户偏好领域模型列表
     */
    @Override
    public List<UserPreference> findByBizId(String bizId) {
        return userPreferenceJpaRepository.findByBizIdAndIsDeleted(bizId, 0)
            .stream()
            .map(this::convertToDomain)
            .collect(Collectors.toList());
    }

    /**
     * 保存用户偏好
     *
     * @param preference 用户偏好领域模型
     */
    @Override
    public void save(UserPreference preference) {
        UserPreferenceEntity entity = convertToEntity(preference);
        userPreferenceJpaRepository.save(entity);
        // 回填生成的ID
        preference.setId(entity.getId());
    }

    /**
     * 更新用户偏好
     *
     * @param preference 用户偏好领域模型
     */
    @Override
    public void update(UserPreference preference) {
        UserPreferenceEntity entity = convertToEntity(preference);
        userPreferenceJpaRepository.save(entity);
    }

    /**
     * 删除用户偏好（逻辑删除）
     *
     * @param id 主键ID
     */
    @Override
    public void deleteById(Long id) {
        // 逻辑删除：设置 is_deleted = 1
        findById(id).ifPresent(preference -> {
            preference.setIsDeleted(1);
            UserPreferenceEntity entity = convertToEntity(preference);
            userPreferenceJpaRepository.save(entity);
        });
    }

    /**
     * 根据业务用户ID删除所有偏好（逻辑删除）
     *
     * @param bizId 业务用户ID
     */
    @Override
    public void deleteByBizId(String bizId) {
        // 使用 Stream API 批量设置逻辑删除标识
        List<UserPreferenceEntity> entities = userPreferenceJpaRepository.findByBizIdAndIsDeleted(bizId, 0);
        entities.forEach(entity -> entity.setIsDeleted(1));
        // 批量更新
        userPreferenceJpaRepository.saveAll(entities);
    }

    /**
     * 领域模型转实体对象
     * <p>
     * 将领域层的 UserPreference 对象转换为基础设施层的 UserPreferenceEntity 对象
     * </p>
     *
     * @param domain 领域模型
     * @return 数据库实体
     */
    private UserPreferenceEntity convertToEntity(UserPreference domain) {
        if (domain == null) {
            return null;
        }
        return BeanUtil.copyProperties(domain, UserPreferenceEntity.class);
    }

    /**
     * 实体对象转领域模型
     * <p>
     * 将基础设施层的 UserPreferenceEntity 对象转换为领域层的 UserPreference 对象
     * </p>
     *
     * @param entity 数据库实体
     * @return 领域模型
     */
    private UserPreference convertToDomain(UserPreferenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, UserPreference.class);
    }
}
