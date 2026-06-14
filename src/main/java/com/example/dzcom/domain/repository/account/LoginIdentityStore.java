package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.LoginIdentity;

import java.util.List;
import java.util.Optional;

/** 登录标识仓储端口。 */
public interface LoginIdentityStore {
    /** 保存登录标识。 */
    LoginIdentity save(LoginIdentity identity);

    /** 根据类型和标准化值查询有效登录标识。 */
    Optional<LoginIdentity> findByTypeAndNormalizedValue(IdentityType type, String normalizedValue);

    /** 根据用户和类型查询登录标识。 */
    Optional<LoginIdentity> findByUserBizIdAndType(String userBizId, IdentityType type);

    /** 查询用户全部有效登录标识。 */
    List<LoginIdentity> findByUserBizId(String userBizId);

    /** 软删除用户全部登录标识。 */
    void softDeleteByUserBizId(String userBizId);
}
