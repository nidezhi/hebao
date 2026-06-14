package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.UserRiskProfile;

import java.util.Optional;

/** 用户风险画像仓储端口。 */
public interface UserRiskProfileStore {
    /** 保存用户风险画像。 */
    UserRiskProfile save(UserRiskProfile profile);

    /** 查询用户有效风险画像。 */
    Optional<UserRiskProfile> findByUserBizId(String userBizId);

    /** 软删除用户风险画像。 */
    void softDeleteByUserBizId(String userBizId);
}
