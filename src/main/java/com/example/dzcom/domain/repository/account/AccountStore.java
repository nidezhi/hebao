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
    /**
     * 创建或保存对应的业务数据。
     *
     * @param user user 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    User saveUser(User user);
    /**
     * 根据指定条件查询业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<User> findUser(String bizId);
    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param criteria 查询筛选条件
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    PageResult<User> searchUsers(UserSearchCriteria criteria);

    /**
     * 创建或保存对应的业务数据。
     *
     * @param identity identity 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    LoginIdentity saveIdentity(LoginIdentity identity);
    /**
     * 根据指定条件查询业务数据。
     *
     * @param type 数据类型
     * @param normalizedValue normalizedValue 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<LoginIdentity> findIdentity(IdentityType type, String normalizedValue);
    /**
     * 根据指定条件查询业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @param type 数据类型
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<LoginIdentity> findIdentity(String userBizId, IdentityType type);
    /**
     * 执行 find identities 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<LoginIdentity> findIdentities(String userBizId);

    /**
     * 创建或保存对应的业务数据。
     *
     * @param credential credential 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    UserCredential saveCredential(UserCredential credential);
    /**
     * 执行 find password credential 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserCredential> findPasswordCredential(String userBizId);

    /**
     * 创建或保存对应的业务数据。
     *
     * @param profile profile 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    UserProfile saveProfile(UserProfile profile);
    /**
     * 根据指定条件查询业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserProfile> findProfile(String userBizId);

    /**
     * 创建或保存对应的业务数据。
     *
     * @param profile profile 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    UserRiskProfile saveRiskProfile(UserRiskProfile profile);
    /**
     * 根据指定条件查询业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserRiskProfile> findRiskProfile(String userBizId);

    /**
     * 创建或保存对应的业务数据。
     *
     * @param role role 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    UserRole saveRole(UserRole role);
    /**
     * 执行 find roles 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserRole> findRoles(String userBizId);

    /**
     * 创建或保存对应的业务数据。
     *
     * @param preference preference 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    UserPreference savePreference(UserPreference preference);
    /**
     * 根据指定条件查询业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @param key 数据键
     * @param includeDeleted includeDeleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserPreference> findPreference(String userBizId, String key, boolean includeDeleted);
    /**
     * 执行 find preferences 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserPreference> findPreferences(String userBizId);

    /**
     * 删除或逻辑删除对应的业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @author dz
     * @date 2026-06-14
     */
    void softDeleteAccountData(String userBizId);
}
