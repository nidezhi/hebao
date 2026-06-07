package com.example.dzcom.infrastructure.session.account;

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

    @Override
    public SessionToken create(String userBizId, int credentialVersion, Set<String> roles) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = hash(token);
        SessionData data = new SessionData(userBizId, credentialVersion, Set.copyOf(roles));
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

    @Override
    public void revokeAll(String userBizId) {
        String userKey = USER_SESSIONS_PREFIX + userBizId;
        Set<String> tokenHashes = redis.opsForSet().members(userKey);
        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            redis.delete(tokenHashes.stream().map(hash -> SESSION_PREFIX + hash).toList());
        }
        redis.delete(userKey);
    }

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
