package com.example.dzcom.infrastructure.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dzcom.infrastructure.dao.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口（Infrastructure层）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
