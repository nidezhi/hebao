package com.example.dzcom.infrastructure.dao.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzcom.domain.model.User;
import com.example.dzcom.domain.repository.UserRepository;
import com.example.dzcom.infrastructure.dao.entity.User as UserEntity;
import com.example.dzcom.infrastructure.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    
    @Override
    public Optional<User> findById(String bizId) {
        UserEntity entity = userMapper.selectById(bizId);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    @Override
    public Optional<User> findByPhone(String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    @Override
    public Optional<User> findByUserNo(String userNo) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUserNo, userNo)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    @Override
    public void save(User user) {
        UserEntity entity = convertToEntity(user);
        userMapper.insert(entity);
        // 回填生成的ID
        user.setBizId(entity.getBizId());
    }
    
    @Override
    public void update(User user) {
        UserEntity entity = convertToEntity(user);
        userMapper.updateById(entity);
    }
    
    @Override
    public void deleteById(String bizId) {
        // 逻辑删除
        UserEntity entity = new UserEntity();
        entity.setBizId(bizId);
        entity.setIsDeleted(1);
        userMapper.updateById(entity);
    }
    
    @Override
    public List<User> findAll(int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, 0)
               .orderByDesc(UserEntity::getCreatedAt);
        
        Page<UserEntity> resultPage = userMapper.selectPage(page, wrapper);
        return resultPage.getRecords().stream()
            .map(this::convertToDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, 0);
        return userMapper.selectCount(wrapper);
    }
    
    /**
     * 领域模型转实体
     */
    private UserEntity convertToEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return BeanUtil.copyProperties(domain, UserEntity.class);
    }
    
    /**
     * 实体转领域模型
     */
    private User convertToDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, User.class);
    }
}
