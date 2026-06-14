package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserProfile;

import java.util.Optional;

public interface UserProfileStore {
    UserProfile save(UserProfile profile);

    Optional<UserProfile> findByUserBizId(String userBizId);

    void softDeleteByUserBizId(String userBizId);
}
