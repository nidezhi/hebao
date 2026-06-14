package com.example.dzcom.infrastructure.config.account.account;

import com.example.dzcom.application.service.account.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的不透明会话实现。
 *
 * <p>Redis 只保存 SHA-256 令牌摘要，原始令牌仅通过 HttpOnly Cookie 返回。
 * 用户维度的会话集合用于在禁用、改密和删除时批量撤销。</p>
 */
@Service
@RequiredArgsConstructor
public class RedisSessionService implements SessionService {
    private static final String SESSION_PREFIX = "dzcom:account:session:";
    private static final String USER_SESSIONS_PREFIX = "dzcom:account:user-sessions:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    @Value("${dzcom.account.session-ttl:7d}")
    private Duration ttl;

    /**
     * 创建或保存对应的业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @param credentialVersion credentialVersion 参数
     * @param roles 有效角色编码
     * @param permissions 有效权限编码
     * @return 方法执行后的结果
     * @throws IllegalStateException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public SessionToken create(String userBizId, int credentialVersion, Set<String> roles,
                               Set<String> permissions) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = hash(token);
        SessionData data = new SessionData(
            userBizId,
            credentialVersion,
            Set.copyOf(roles),
            Set.copyOf(permissions)
        );
        try {
            redis.opsForValue().set(SESSION_PREFIX + tokenHash, objectMapper.writeValueAsString(data), ttl);
            String userKey = USER_SESSIONS_PREFIX + userBizId;
            redis.opsForSet().add(userKey, tokenHash);
            redis.expire(userKey, ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("会话序列化失败", e);
        }
        return new SessionToken(token, ttl.toSeconds());
    }

    /**
     * 根据指定条件查询业务数据。
     *
     * @param token token 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public Optional<SessionData> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String json = redis.opsForValue().get(SESSION_PREFIX + hash(token));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SessionData.class));
        } catch (JsonProcessingException e) {
            revoke(token);
            return Optional.empty();
        }
    }

    /**
     * 执行 revoke 处理。
     *
     * @param token token 参数
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String tokenHash = hash(token);
        resolveWithoutRecursion(tokenHash).ifPresent(data ->
            redis.opsForSet().remove(USER_SESSIONS_PREFIX + data.userBizId(), tokenHash));
        redis.delete(SESSION_PREFIX + tokenHash);
    }

    /**
     * 执行 revoke all 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public void revokeAll(String userBizId) {
        String userKey = USER_SESSIONS_PREFIX + userBizId;
        Set<String> tokenHashes = redis.opsForSet().members(userKey);
        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            redis.delete(tokenHashes.stream().map(hash -> SESSION_PREFIX + hash).toList());
        }
        redis.delete(userKey);
    }

    /**
     * 执行 resolve without recursion 处理。
     *
     * @param tokenHash tokenHash 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private Optional<SessionData> resolveWithoutRecursion(String tokenHash) {
        String json = redis.opsForValue().get(SESSION_PREFIX + tokenHash);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SessionData.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    /**
     * 计算输入内容的安全哈希值。
     *
     * @param token token 参数
     * @return 方法执行后的结果
     * @throws IllegalStateException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM 不支持 SHA-256", e);
        }
    }
}
