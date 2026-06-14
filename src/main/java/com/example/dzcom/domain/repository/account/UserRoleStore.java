package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserRole;

import java.util.List;
import java.util.Optional;

/** 用户角色仓储端口。 */
public interface UserRoleStore {
    /** 保存用户角色。 */
    UserRole save(UserRole role);

    /** 查询用户全部有效角色。 */
    List<UserRole> findByUserBizId(String userBizId);

    /** 查询用户指定角色分配，包含已撤销记录。 */
    Optional<UserRole> findByUserBizIdAndRoleCode(String userBizId, String roleCode);

    /** 查询持有指定角色的用户业务标识。 */
    List<String> findUserBizIdsByRoleCode(String roleCode);

    /** 统计通过其他角色具备指定权限的有效用户数量。 */
    long countUsersWithPermissionExcludingRole(String permissionCode, String excludedRoleCode);

    /** 统计排除指定用户角色分配后仍具备权限的有效用户数量。 */
    long countUsersWithPermissionExcludingAssignment(String permissionCode, String userBizId,
                                                      String roleCode);

    /** 软删除用户的指定角色。 */
    void softDelete(String userBizId, String roleCode);

    /** 软删除用户全部角色。 */
    void softDeleteByUserBizId(String userBizId);
}
