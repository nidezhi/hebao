package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserRole;

import java.util.List;

public interface UserRoleStore {
    UserRole save(UserRole role);

    List<UserRole> findByUserBizId(String userBizId);

    void softDeleteByUserBizId(String userBizId);
}
