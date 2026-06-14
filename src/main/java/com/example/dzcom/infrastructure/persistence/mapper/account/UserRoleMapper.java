package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 用户角色 MyBatis Mapper。 */
@Mapper
public interface UserRoleMapper {
    /** 根据业务标识查询用户角色。 */
    UserRoleEntity selectById(@Param("bizId") String bizId);

    /** 根据用户业务标识查询角色列表。 */
    List<UserRoleEntity> selectByUserBizId(@Param("userBizId") String userBizId,
                                           @Param("includeDeleted") boolean includeDeleted);

    /** 新增或更新用户角色。 */
    int save(UserRoleEntity entity);

    /** 软删除用户的全部角色。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
