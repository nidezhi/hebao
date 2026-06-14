package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserProfile;

import java.util.Optional;

/** 用户资料仓储端口。 */
public interface UserProfileStore {
    /** 保存用户资料。 */
    UserProfile save(UserProfile profile);

    /** 查询用户有效资料。 */
    Optional<UserProfile> findByUserBizId(String userBizId);

    /** 软删除用户资料。 */
    void softDeleteByUserBizId(String userBizId);
}
