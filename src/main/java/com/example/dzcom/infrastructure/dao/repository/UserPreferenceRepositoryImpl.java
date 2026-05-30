package com.example.dzcom.infrastructure.dao.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dzcom.domain.model.UserPreference;
import com.example.dzcom.domain.repository.UserPreferenceRepository;
import com.example.dzcom.infrastructure.dao.entity.UserPreferenceEntity;
import com.example.dzcom.infrastructure.dao.mapper.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户偏好仓储实现（Infrastructure层）
 * <p>
 * 实现领域层定义的 UserPreferenceRepository 接口，负责用户偏好数据的持久化操作
 * 使用 MyBatis-Plus 进行数据库访问，完成实体与领域模型的转换
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
     * 用户偏好数据访问接口
     */
    private final UserPreferenceMapper userPreferenceMapper;
    
    /**
     * 根据主键ID查询用户偏好
     *
     * @param id 主键ID
     * @return 用户偏好领域模型（可选）
     */
    @Override
    public Optional<UserPreference> findById(Long id) {
        UserPreferenceEntity entity = userPreferenceMapper.selectById(id);
        return Optional.ofNullable(convertToDomain(entity));
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
        LambdaQueryWrapper<UserPreferenceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreferenceEntity::getBizId, bizId)
               .eq(UserPreferenceEntity::getPreferenceKey, preferenceKey)
               .eq(UserPreferenceEntity::getIsDeleted, 0);
        UserPreferenceEntity entity = userPreferenceMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    /**
     * 根据业务用户ID查询所有偏好
     *
     * @param bizId 业务用户ID
     * @return 用户偏好领域模型列表
     */
    @Override
    public List<UserPreference> findByBizId(String bizId) {
        LambdaQueryWrapper<UserPreferenceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreferenceEntity::getBizId, bizId)
               .eq(UserPreferenceEntity::getIsDeleted, 0);
        List<UserPreferenceEntity> entities = userPreferenceMapper.selectList(wrapper);
        return entities.stream()
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
        userPreferenceMapper.insert(entity);
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
        userPreferenceMapper.updateById(entity);
    }
    
    /**
     * 删除用户偏好（逻辑删除）
     *
     * @param id 主键ID
     */
    @Override
    public void deleteById(Long id) {
        // 逻辑删除：设置 is_deleted = 1
        UserPreferenceEntity entity = new UserPreferenceEntity();
        entity.setId(id);
        entity.setIsDeleted(1);
        userPreferenceMapper.updateById(entity);
    }
    
    /**
     * 根据业务用户ID删除所有偏好（逻辑删除）
     *
     * @param bizId 业务用户ID
     */
    @Override
    public void deleteByBizId(String bizId) {
        LambdaQueryWrapper<UserPreferenceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreferenceEntity::getBizId, bizId);
        
        List<UserPreferenceEntity> entities = userPreferenceMapper.selectList(wrapper);
        for (UserPreferenceEntity entity : entities) {
            entity.setIsDeleted(1);
            userPreferenceMapper.updateById(entity);
        }
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
