package com.example.dzcom.domain.repository.account;

import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.*;

import java.util.List;
import java.util.Optional;

/**
 * 账户领域统一仓储端口。
 *
 * <p>应用服务只依赖该接口，不感知 JPA、SQL 和 Entity。由于数据库不使用外键，
 * 多表写入和删除必须由应用服务在事务中显式协调。</p>
 */
public interface AccountStore {
    User saveUser(User user);
    Optional<User> findUser(String bizId);
    PageResult<User> searchUsers(UserSearchCriteria criteria);

    LoginIdentity saveIdentity(LoginIdentity identity);
    Optional<LoginIdentity> findIdentity(IdentityType type, String normalizedValue);
    Optional<LoginIdentity> findIdentity(String userBizId, IdentityType type);
    List<LoginIdentity> findIdentities(String userBizId);

    UserCredential saveCredential(UserCredential credential);
    Optional<UserCredential> findPasswordCredential(String userBizId);

    UserProfile saveProfile(UserProfile profile);
    Optional<UserProfile> findProfile(String userBizId);

    UserRiskProfile saveRiskProfile(UserRiskProfile profile);
    Optional<UserRiskProfile> findRiskProfile(String userBizId);

    UserRole saveRole(UserRole role);
    List<UserRole> findRoles(String userBizId);

    UserPreference savePreference(UserPreference preference);
    Optional<UserPreference> findPreference(String userBizId, String key, boolean includeDeleted);
    List<UserPreference> findPreferences(String userBizId);

    void softDeleteAccountData(String userBizId);
}
