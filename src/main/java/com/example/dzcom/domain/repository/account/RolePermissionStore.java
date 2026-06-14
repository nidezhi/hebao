package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.RolePermission;

import java.util.List;
import java.util.Set;

/** 角色权限映射仓储端口。 */
public interface RolePermissionStore {
    /** 保存角色权限映射。 */
    RolePermission save(RolePermission permission);

    /** 查询角色全部有效权限映射。 */
    List<RolePermission> findByRoleCode(String roleCode);

    /** 查询多个角色合并后的有效权限编码。 */
    Set<String> findPermissionCodesByRoleCodes(Set<String> roleCodes);

    /** 软删除角色的全部权限映射。 */
    void softDeleteByRoleCode(String roleCode);
}
