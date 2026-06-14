package com.example.dzcom.application.service.account;

import java.util.Optional;
import java.util.Set;

/**
 * 可撤销的不透明会话服务。
 *
 * <p>外部只持有随机令牌，服务端保存令牌摘要和会话数据。改密、禁用、
 * 锁定和删除账户时必须通过 {@link #revokeAll(String)} 撤销历史会话。</p>
 */
public interface SessionService {
    /**
     * 为用户创建新会话并返回仅此一次可写入 Cookie 的原始令牌。
     *
     * @param userBizId 业务对象的唯一标识
     * @param credentialVersion credentialVersion 参数
     * @param roles 有效角色编码
     * @param permissions 有效权限编码
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    SessionToken create(String userBizId, int credentialVersion, Set<String> roles,
                        Set<String> permissions);

    /**
     * 解析会话令牌；令牌不存在、过期或数据损坏时返回空。
     *
     * @param token token 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<SessionData> resolve(String token);

    /**
     * 撤销指定会话，重复调用保持幂等。
     *
     * @param token token 参数
     * @author dz
     * @date 2026-06-14
     */
    void revoke(String token);

    /**
     * 撤销指定用户的全部会话。
     *
     * @param userBizId 业务对象的唯一标识
     * @author dz
     * @date 2026-06-14
     */
    void revokeAll(String userBizId);

    /** 新创建会话的原始令牌及 Cookie 最大存活秒数。 */
    record SessionToken(String value, long maxAgeSeconds) {
    }

    /** 服务端保存的最小会话数据，不包含密码和个人敏感信息。 */
    record SessionData(
        String userBizId,
        int credentialVersion,
        Set<String> roles,
        Set<String> permissions
    ) {
        public SessionData {
            roles = roles == null ? Set.of() : Set.copyOf(roles);
            permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        }
    }
}
