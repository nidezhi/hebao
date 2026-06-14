package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserCredential;

import java.util.Optional;

public interface UserCredentialStore {
    UserCredential save(UserCredential credential);

    Optional<UserCredential> findPasswordByUserBizId(String userBizId);

    void softDeleteByUserBizId(String userBizId);
}
