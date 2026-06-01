package com.example.dzcom.infrastructure.dao.repository;

import com.example.dzcom.infrastructure.dao.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户JPA仓储接口（Infrastructure层）
 * <p>
 * 继承 JpaRepository 和 JpaSpecificationExecutor，提供基础的 CRUD 操作和动态查询能力
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体（可选）
     */
    Optional<UserEntity> findByUsernameAndIsDeleted(String username, Integer isDeleted);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户实体（可选）
     */
    Optional<UserEntity> findByEmailAndIsDeleted(String email, Integer isDeleted);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号码
     * @return 用户实体（可选）
     */
    Optional<UserEntity> findByPhoneAndIsDeleted(String phone, Integer isDeleted);

    /**
     * 根据用户编号查询用户
     *
     * @param userNo 用户编号
     * @return 用户实体（可选）
     */
    Optional<UserEntity> findByUserNoAndIsDeleted(String userNo, Integer isDeleted);

    /**
     * 统计未删除的用户数量
     *
     * @param isDeleted 删除标识
     * @return 用户数量
     */
    long countByIsDeleted(Integer isDeleted);

    /**
     * 查询所有未删除的用户，按创建时间倒序
     *
     * @param isDeleted 删除标识
     * @return 用户实体列表
     */
    List<UserEntity> findAllByIsDeletedOrderByCreatedAtDesc(Integer isDeleted);
}
