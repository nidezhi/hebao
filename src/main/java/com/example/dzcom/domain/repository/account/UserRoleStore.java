package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserRole;

import java.util.List;

/** 用户角色仓储端口。 */
public interface UserRoleStore {
    /** 保存用户角色。 */
    UserRole save(UserRole role);

    /** 查询用户全部有效角色。 */
    List<UserRole> findByUserBizId(String userBizId);

    /** 软删除用户全部角色。 */
    void softDeleteByUserBizId(String userBizId);
}
