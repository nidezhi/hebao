package com.example.dzcom.infrastructure.dao.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dzcom.domain.model.UserPreference;
import com.example.dzcom.domain.repository.UserPreferenceRepository;
import com.example.dzcom.infrastructure.dao.entity.UserPreference as UserPreferenceEntity;
import com.example.dzcom.infrastructure.dao.mapper.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户偏好仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserPreferenceRepositoryImpl implements UserPreferenceRepository {
    
    private final UserPreferenceMapper userPreferenceMapper;
    
    @Override
    public Optional<UserPreference> findById(Long id) {
        UserPreferenceEntity entity = userPreferenceMapper.selectById(id);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    @Override
    public Optional<UserPreference> findByBizIdAndKey(String bizId, String preferenceKey) {
        LambdaQueryWrapper<UserPreferenceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreferenceEntity::getBizId, bizId)
               .eq(UserPreferenceEntity::getPreferenceKey, preferenceKey)
               .eq(UserPreferenceEntity::getIsDeleted, 0);
        UserPreferenceEntity entity = userPreferenceMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
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
    
    @Override
    public void save(UserPreference preference) {
        UserPreferenceEntity entity = convertToEntity(preference);
        userPreferenceMapper.insert(entity);
        preference.setId(entity.getId());
    }
    
    @Override
    public void update(UserPreference preference) {
        UserPreferenceEntity entity = convertToEntity(preference);
        userPreferenceMapper.updateById(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        // 逻辑删除
        UserPreferenceEntity entity = new UserPreferenceEntity();
        entity.setId(id);
        entity.setIsDeleted(1);
        userPreferenceMapper.updateById(entity);
    }
    
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
     * 领域模型转实体
     */
    private UserPreferenceEntity convertToEntity(UserPreference domain) {
        if (domain == null) {
            return null;
        }
        return BeanUtil.copyProperties(domain, UserPreferenceEntity.class);
    }
    
    /**
     * 实体转领域模型
     */
    private UserPreference convertToDomain(UserPreferenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, UserPreference.class);
    }
}
