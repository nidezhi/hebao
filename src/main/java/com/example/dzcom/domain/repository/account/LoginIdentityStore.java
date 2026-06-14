package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.LoginIdentity;

import java.util.List;
import java.util.Optional;

public interface LoginIdentityStore {
    LoginIdentity save(LoginIdentity identity);

    Optional<LoginIdentity> findByTypeAndNormalizedValue(IdentityType type, String normalizedValue);

    Optional<LoginIdentity> findByUserBizIdAndType(String userBizId, IdentityType type);

    List<LoginIdentity> findByUserBizId(String userBizId);

    void softDeleteByUserBizId(String userBizId);
}
