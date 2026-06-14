package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.model.account.UserCredential;
import com.example.dzcom.domain.repository.account.UserCredentialStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserCredentialEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.UserCredentialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户凭据仓储实现，直接维护密码凭据的持久化和领域转换。
 */
@Repository
@RequiredArgsConstructor
public class UserCredentialStoreImpl implements UserCredentialStore {
    /** MyBatis 用户凭据执行器。 */
    private final UserCredentialMapper mapper;

    /**
     * 保存密码凭据。
     *
     * @param value 用户凭据领域对象
     * @return 保存后的用户凭据
     */
    @Override
    public UserCredential save(UserCredential value) {
        UserCredentialEntity existing = mapper.selectById(value.bizId());
        LocalDateTime createdAt = Optional.ofNullable(existing)
            .map(UserCredentialEntity::getCreatedAt)
            .orElse(value.changedAt());
        UserCredentialEntity entity = Optional.ofNullable(existing)
            .map(UserCredentialEntity::toBuilder)
            .orElseGet(UserCredentialEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .credentialType("PASSWORD")
            .secretHash(value.secretHash())
            .hashAlgorithm(value.hashAlgorithm())
            .credentialVersion(value.credentialVersion())
            .changedAt(value.changedAt())
            .failedAttempts(value.failedAttempts())
            .lockedUntil(value.lockedUntil())
            .createdAt(createdAt)
            .updatedAt(value.changedAt())
            .deleted(value.deleted())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 查询用户未删除的密码凭据。
     *
     * @param userBizId 用户业务标识
     * @return 密码凭据
     */
    @Override
    public Optional<UserCredential> findPasswordByUserBizId(String userBizId) {
        return Optional.ofNullable(mapper.selectPasswordByUserBizId(userBizId))
            .map(this::toDomain);
    }

    /**
     * 软删除用户全部凭据。
     *
     * @param userBizId 用户业务标识
     */
    @Override
    public void softDeleteByUserBizId(String userBizId) {
        mapper.softDeleteByUserBizId(userBizId);
    }

    /**
     * 将凭据实体转换为领域对象。
     *
     * @param entity 凭据实体
     * @return 凭据领域对象
     */
    private UserCredential toDomain(UserCredentialEntity entity) {
        return UserCredential.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .secretHash(entity.getSecretHash())
            .hashAlgorithm(entity.getHashAlgorithm())
            .credentialVersion(entity.getCredentialVersion())
            .failedAttempts(entity.getFailedAttempts())
            .lockedUntil(entity.getLockedUntil())
            .changedAt(entity.getChangedAt())
            .deleted(entity.getDeleted())
            .build();
    }
}
