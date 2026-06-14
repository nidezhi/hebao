package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserPreference;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceStore {
    UserPreference save(UserPreference preference);

    Optional<UserPreference> findByUserBizIdAndKey(String userBizId, String key, boolean includeDeleted);

    List<UserPreference> findByUserBizId(String userBizId);

    void softDeleteByUserBizId(String userBizId);
}
