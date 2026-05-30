package com.example.dzcom.infrastructure.dao.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzcom.domain.model.User;
import com.example.dzcom.domain.repository.UserRepository;
import com.example.dzcom.infrastructure.dao.entity.UserEntity;
import com.example.dzcom.infrastructure.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓储实现（Infrastructure层）
 * <p>
 * 实现领域层定义的 UserRepository 接口，负责用户数据的持久化操作
 * 使用 MyBatis-Plus 进行数据库访问，完成实体与领域模型的转换
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    /**
     * 用户数据访问接口
     */
    private final UserMapper userMapper;
    
    /**
     * 根据业务ID查询用户
     *
     * @param bizId 业务用户ID
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findById(String bizId) {
        UserEntity entity = userMapper.selectById(bizId);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByEmail(String email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号码
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByPhone(String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    /**
     * 根据用户编号查询用户
     *
     * @param userNo 用户编号
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByUserNo(String userNo) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUserNo, userNo)
               .eq(UserEntity::getIsDeleted, 0);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(convertToDomain(entity));
    }
    
    /**
     * 保存新用户
     *
     * @param user 用户领域模型
     */
    @Override
    public void save(User user) {
        UserEntity entity = convertToEntity(user);
        userMapper.insert(entity);
        // 回填生成的ID
        user.setBizId(entity.getBizId());
    }
    
    /**
     * 更新用户信息
     *
     * @param user 用户领域模型
     */
    @Override
    public void update(User user) {
        UserEntity entity = convertToEntity(user);
        userMapper.updateById(entity);
    }
    
    /**
     * 删除用户（逻辑删除）
     *
     * @param bizId 业务用户ID
     */
    @Override
    public void deleteById(String bizId) {
        // 逻辑删除：设置 is_deleted = 1
        UserEntity entity = new UserEntity();
        entity.setBizId(bizId);
        entity.setIsDeleted(1);
        userMapper.updateById(entity);
    }
    
    /**
     * 分页查询所有用户
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 用户领域模型列表
     */
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
    
    /**
     * 统计用户总数
     *
     * @return 未删除的用户数量
     */
    @Override
    public long count() {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, 0);
        return userMapper.selectCount(wrapper);
    }
    
    /**
     * 领域模型转实体对象
     * <p>
     * 将领域层的 User 对象转换为基础设施层的 UserEntity 对象
     * </p>
     *
     * @param domain 领域模型
     * @return 数据库实体
     */
    private UserEntity convertToEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return BeanUtil.copyProperties(domain, UserEntity.class);
    }
    
    /**
     * 实体对象转领域模型
     * <p>
     * 将基础设施层的 UserEntity 对象转换为领域层的 User 对象
     * </p>
     *
     * @param entity 数据库实体
     * @return 领域模型
     */
    private User convertToDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, User.class);
    }
}
