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

    /** 查询用户指定角色分配。 */
    UserRoleEntity selectByUserBizIdAndRoleCode(@Param("userBizId") String userBizId,
                                                @Param("roleCode") String roleCode);

    /** 查询持有指定角色的用户业务标识。 */
    List<String> selectUserBizIdsByRoleCode(@Param("roleCode") String roleCode);

    /** 统计通过其他角色具备指定权限的有效用户数量。 */
    long countUsersWithPermissionExcludingRole(@Param("permissionCode") String permissionCode,
                                               @Param("excludedRoleCode") String excludedRoleCode);

    /** 新增或更新用户角色。 */
    int save(UserRoleEntity entity);

    /** 软删除用户的指定角色。 */
    int softDelete(@Param("userBizId") String userBizId, @Param("roleCode") String roleCode);

    /** 软删除用户的全部角色。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
