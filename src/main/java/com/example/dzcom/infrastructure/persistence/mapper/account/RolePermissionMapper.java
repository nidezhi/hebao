package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.RolePermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/** 角色权限映射 MyBatis Mapper。 */
@Mapper
public interface RolePermissionMapper {
    /** 根据角色编码和权限编码查询映射。 */
    RolePermissionEntity selectOne(@Param("roleCode") String roleCode,
                                   @Param("permissionCode") String permissionCode);

    /** 查询角色全部有效权限映射。 */
    List<RolePermissionEntity> selectByRoleCode(@Param("roleCode") String roleCode);

    /** 查询多个角色合并后的有效权限编码。 */
    Set<String> selectPermissionCodesByRoleCodes(@Param("roleCodes") Set<String> roleCodes);

    /** 新增或更新角色权限映射。 */
    int save(RolePermissionEntity entity);

    /** 软删除角色的全部权限映射。 */
    int softDeleteByRoleCode(@Param("roleCode") String roleCode);
}
