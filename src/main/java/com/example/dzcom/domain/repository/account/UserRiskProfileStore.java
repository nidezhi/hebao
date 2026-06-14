package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserRiskProfile;

import java.util.Optional;

public interface UserRiskProfileStore {
    UserRiskProfile save(UserRiskProfile profile);

    Optional<UserRiskProfile> findByUserBizId(String userBizId);

    void softDeleteByUserBizId(String userBizId);
}
