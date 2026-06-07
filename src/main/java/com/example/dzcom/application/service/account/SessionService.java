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
    /** 为用户创建新会话并返回仅此一次可写入 Cookie 的原始令牌。 */
    SessionToken create(String userBizId, int credentialVersion, Set<String> roles);

    /** 解析会话令牌；令牌不存在、过期或数据损坏时返回空。 */
    Optional<SessionData> resolve(String token);

    /** 撤销指定会话，重复调用保持幂等。 */
    void revoke(String token);

    /** 撤销指定用户的全部会话。 */
    void revokeAll(String userBizId);

    /** 新创建会话的原始令牌及 Cookie 最大存活秒数。 */
    record SessionToken(String value, long maxAgeSeconds) {
    }

    /** 服务端保存的最小会话数据，不包含密码和个人敏感信息。 */
    record SessionData(String userBizId, int credentialVersion, Set<String> roles) {
    }
}
