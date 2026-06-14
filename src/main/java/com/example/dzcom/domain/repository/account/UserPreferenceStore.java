package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserPreference;

import java.util.List;
import java.util.Optional;

/** 用户偏好仓储端口。 */
public interface UserPreferenceStore {
    /** 保存用户偏好。 */
    UserPreference save(UserPreference preference);

    /** 根据用户和偏好键查询偏好。 */
    Optional<UserPreference> findByUserBizIdAndKey(String userBizId, String key, boolean includeDeleted);

    /** 查询用户全部有效偏好。 */
    List<UserPreference> findByUserBizId(String userBizId);

    /** 软删除用户全部偏好。 */
    void softDeleteByUserBizId(String userBizId);
}
