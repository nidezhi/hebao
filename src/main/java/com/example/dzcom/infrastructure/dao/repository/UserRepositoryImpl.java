package com.example.dzcom.infrastructure.dao.repository;

import cn.hutool.core.bean.BeanUtil;
import com.example.dzcom.domain.model.User;
import com.example.dzcom.domain.repository.UserRepository;
import com.example.dzcom.infrastructure.dao.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓储实现（Infrastructure层）
 * <p>
 * 实现领域层定义的 UserRepository 接口，负责用户数据的持久化操作
 * 使用 Spring Data JPA 进行数据库访问，完成实体与领域模型的转换
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
     * 用户JPA仓储接口
     */
    private final UserJpaRepository userJpaRepository;

    /**
     * 根据业务ID查询用户
     *
     * @param bizId 业务用户ID
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findById(String bizId) {
        return userJpaRepository.findById(bizId)
            .map(this::convertToDomain);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsernameAndIsDeleted(username, 0)
            .map(this::convertToDomain);
    }

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmailAndIsDeleted(email, 0)
            .map(this::convertToDomain);
    }

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号码
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByPhone(String phone) {
        return userJpaRepository.findByPhoneAndIsDeleted(phone, 0)
            .map(this::convertToDomain);
    }

    /**
     * 根据用户编号查询用户
     *
     * @param userNo 用户编号
     * @return 用户领域模型（可选）
     */
    @Override
    public Optional<User> findByUserNo(String userNo) {
        return userJpaRepository.findByUserNoAndIsDeleted(userNo, 0)
            .map(this::convertToDomain);
    }

    /**
     * 保存新用户
     *
     * @param user 用户领域模型
     */
    @Override
    public void save(User user) {
        UserEntity entity = convertToEntity(user);
        userJpaRepository.save(entity);
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
        userJpaRepository.save(entity);
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param bizId 业务用户ID
     */
    @Override
    public void deleteById(String bizId) {
        // 逻辑删除：设置 is_deleted = 1
        findById(bizId).ifPresent(user -> {
            user.setIsDeleted(1);
            UserEntity entity = convertToEntity(user);
            userJpaRepository.save(entity);
        });
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
        PageRequest pageRequest = PageRequest.of(
            pageNum - 1, 
            pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        return userJpaRepository.findAllByIsDeletedOrderByCreatedAtDesc(0)
            .stream()
            .skip((long) (pageNum - 1) * pageSize)
            .limit(pageSize)
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
        return userJpaRepository.countByIsDeleted(0);
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
