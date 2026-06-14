package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserCredential;

import java.util.Optional;

/** 用户凭据仓储端口。 */
public interface UserCredentialStore {
    /** 保存用户凭据。 */
    UserCredential save(UserCredential credential);

    /** 查询用户有效密码凭据。 */
    Optional<UserCredential> findPasswordByUserBizId(String userBizId);

    /** 软删除用户全部凭据。 */
    void softDeleteByUserBizId(String userBizId);
}
