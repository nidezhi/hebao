package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.infrastructure.persistence.entity.account.*;
import com.example.dzcom.domain.model.account.*;
import com.example.dzcom.domain.repository.account.AccountStore;
import com.example.dzcom.domain.repository.account.UserSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 账户领域仓储的 JPA 适配器。
 *
 * <p>该类负责领域对象与持久化实体之间的显式转换。所有集合映射使用 Stream，
 * 所有对象组装使用 Builder，不使用反射复制，也不建立 JPA 级联关系。</p>
 */
@Repository
@RequiredArgsConstructor
public class AccountStoreAdapter implements AccountStore {
    private final JpaUserRepository users;
    private final JpaUserIdentityRepository identities;
    private final JpaUserCredentialRepository credentials;
    private final JpaUserProfileRepository profiles;
    private final JpaUserRiskProfileRepository riskProfiles;
    private final JpaUserRoleRepository roles;
    private final JpaUserPreferenceRepository preferences;

    @Override
    public User saveUser(User user) {
        UserEntity.UserEntityBuilder entityBuilder = users.findById(user.getBizId())
            .map(UserEntity::toBuilder)
            .orElseGet(UserEntity::builder);
        UserEntity entity = entityBuilder
            .bizId(user.getBizId())
            .userNo(user.getUserNo())
            .status(user.getStatus().code())
            .version(user.getVersion())
            .registeredAt(user.getRegisteredAt())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deleted(user.getDeleted())
            .deletedAt(user.getDeletedAt())
            .build();
        return toDomain(users.save(entity));
    }

    @Override
    public Optional<User> findUser(String bizId) {
        return users.findByBizIdAndDeleted(bizId, 0).map(this::toDomain);
    }

    @Override
    public PageResult<User> searchUsers(UserSearchCriteria criteria) {
        Sort sort = Sort.by(criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, criteria.sort());
        PageRequest pageable = PageRequest.of(criteria.page() - 1, criteria.size(), sort);
        Page<UserEntity> page = users.search(blankToNull(criteria.keyword()),
            criteria.status() == null ? null : criteria.status().code(),
            criteria.kycStatus() == null ? null : criteria.kycStatus().code(),
            criteria.riskLevel(), pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getTotalElements(),
            criteria.page(),
            criteria.size(),
            page.getTotalPages()
        );
    }

    @Override
    public LoginIdentity saveIdentity(LoginIdentity value) {
        UserIdentityEntity entity = identities.findById(value.bizId())
            .map(UserIdentityEntity::toBuilder)
            .orElseGet(UserIdentityEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .identityType(value.type().name())
            .identityValue(value.value())
            .normalizedValue(value.normalizedValue())
            .verified(value.verified())
            .status(value.active() ? 1 : 0)
            .createdAt(value.createdAt())
            .updatedAt(LocalDateTime.now())
            .deleted(value.deleted())
            .build();
        return toDomain(identities.save(entity));
    }

    @Override
    public Optional<LoginIdentity> findIdentity(IdentityType type, String normalizedValue) {
        return identities.findByIdentityTypeAndNormalizedValueAndStatusAndDeleted(
                type.name(), normalizedValue, 1, 0)
            .map(this::toDomain);
    }

    @Override
    public Optional<LoginIdentity> findIdentity(String userBizId, IdentityType type) {
        return identities.findByUserBizIdAndIdentityTypeAndDeleted(userBizId, type.name(), 0)
            .map(this::toDomain);
    }

    @Override
    public List<LoginIdentity> findIdentities(String userBizId) {
        return identities.findAllByUserBizIdAndDeleted(userBizId, 0)
            .stream().map(this::toDomain).toList();
    }

    @Override
    public UserCredential saveCredential(UserCredential value) {
        LocalDateTime createdAt = credentials.findById(value.bizId())
            .map(UserCredentialEntity::getCreatedAt)
            .orElse(value.changedAt());
        UserCredentialEntity entity = credentials.findById(value.bizId())
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
        return toDomain(credentials.save(entity));
    }

    @Override
    public Optional<UserCredential> findPasswordCredential(String userBizId) {
        return credentials.findByUserBizIdAndCredentialTypeAndDeleted(userBizId, "PASSWORD", 0)
            .map(this::toDomain);
    }

    @Override
    public UserProfile saveProfile(UserProfile value) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = profiles.findById(value.bizId())
            .map(UserProfileEntity::getCreatedAt)
            .orElse(now);
        UserProfileEntity entity = profiles.findById(value.bizId())
            .map(UserProfileEntity::toBuilder)
            .orElseGet(UserProfileEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .nickname(value.nickname())
            .avatarUrl(value.avatarUrl())
            .locale(value.locale())
            .timezone(value.timezone())
            .createdAt(createdAt)
            .updatedAt(now)
            .deleted(value.deleted())
            .build();
        return toDomain(profiles.save(entity));
    }

    @Override
    public Optional<UserProfile> findProfile(String userBizId) {
        return profiles.findByUserBizIdAndDeleted(userBizId, 0).map(this::toDomain);
    }

    @Override
    public UserRiskProfile saveRiskProfile(UserRiskProfile value) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = riskProfiles.findById(value.bizId())
            .map(UserRiskProfileEntity::getCreatedAt)
            .orElse(now);
        UserRiskProfileEntity entity = riskProfiles.findById(value.bizId())
            .map(UserRiskProfileEntity::toBuilder)
            .orElseGet(UserRiskProfileEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .kycStatus(value.kycStatus().code())
            .riskLevel(value.riskLevel())
            .assessmentVersion(value.assessmentVersion())
            .assessedAt(value.assessedAt())
            .kycReviewedAt(value.kycReviewedAt())
            .createdAt(createdAt)
            .updatedAt(now)
            .deleted(value.deleted())
            .build();
        return toDomain(riskProfiles.save(entity));
    }

    @Override
    public Optional<UserRiskProfile> findRiskProfile(String userBizId) {
        return riskProfiles.findByUserBizIdAndDeleted(userBizId, 0).map(this::toDomain);
    }

    @Override
    public UserRole saveRole(UserRole value) {
        LocalDateTime createdAt = roles.findById(value.bizId())
            .map(UserRoleEntity::getCreatedAt)
            .orElse(value.effectiveFrom());
        UserRoleEntity entity = roles.findById(value.bizId())
            .map(UserRoleEntity::toBuilder)
            .orElseGet(UserRoleEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .roleCode(value.roleCode())
            .scopeCode(value.scopeCode())
            .effectiveFrom(value.effectiveFrom())
            .effectiveTo(value.effectiveTo())
            .createdAt(createdAt)
            .deleted(value.deleted())
            .build();
        return toDomain(roles.save(entity));
    }

    @Override
    public List<UserRole> findRoles(String userBizId) {
        return roles.findAllByUserBizIdAndDeleted(userBizId, 0).stream().map(this::toDomain).toList();
    }

    @Override
    public UserPreference savePreference(UserPreference value) {
        LocalDateTime createdAt = preferences.findById(value.bizId())
            .map(UserPreferenceEntity::getCreatedAt)
            .orElse(value.updatedAt());
        UserPreferenceEntity entity = preferences.findById(value.bizId())
            .map(UserPreferenceEntity::toBuilder)
            .orElseGet(UserPreferenceEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .preferenceKey(value.key())
            .valueType(value.valueType())
            .preferenceValue(value.jsonValue())
            .createdAt(createdAt)
            .updatedAt(value.updatedAt())
            .deleted(value.deleted())
            .build();
        return toDomain(preferences.save(entity));
    }

    @Override
    public Optional<UserPreference> findPreference(String userBizId, String key, boolean includeDeleted) {
        Optional<UserPreferenceEntity> result = includeDeleted
            ? preferences.findByUserBizIdAndPreferenceKey(userBizId, key)
            : preferences.findByUserBizIdAndPreferenceKeyAndDeleted(userBizId, key, 0);
        return result.map(this::toDomain);
    }

    @Override
    public List<UserPreference> findPreferences(String userBizId) {
        return preferences.findAllByUserBizIdAndDeletedOrderByPreferenceKey(userBizId, 0)
            .stream().map(this::toDomain).toList();
    }

    @Override
    public void softDeleteAccountData(String userBizId) {
        identities.findAllByUserBizId(userBizId).forEach(entity -> entity.setDeleted(1));
        credentials.findAllByUserBizId(userBizId).forEach(entity -> entity.setDeleted(1));
        profiles.findByUserBizId(userBizId).ifPresent(entity -> entity.setDeleted(1));
        riskProfiles.findByUserBizId(userBizId).ifPresent(entity -> entity.setDeleted(1));
        roles.findAllByUserBizId(userBizId).forEach(entity -> entity.setDeleted(1));
        preferences.findAllByUserBizId(userBizId).forEach(entity -> entity.setDeleted(1));
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
            .bizId(entity.getBizId())
            .userNo(entity.getUserNo())
            .status(AccountStatus.fromCode(entity.getStatus()))
            .version(entity.getVersion())
            .registeredAt(entity.getRegisteredAt())
            .lastLoginAt(entity.getLastLoginAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deleted(entity.getDeleted())
            .deletedAt(entity.getDeletedAt())
            .build();
    }

    private LoginIdentity toDomain(UserIdentityEntity entity) {
        return LoginIdentity.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .type(IdentityType.valueOf(entity.getIdentityType()))
            .value(entity.getIdentityValue())
            .normalizedValue(entity.getNormalizedValue())
            .verified(entity.isVerified())
            .active(entity.getStatus() == 1)
            .createdAt(entity.getCreatedAt())
            .deleted(entity.getDeleted())
            .build();
    }

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

    private UserProfile toDomain(UserProfileEntity entity) {
        return UserProfile.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .nickname(entity.getNickname())
            .avatarUrl(entity.getAvatarUrl())
            .locale(entity.getLocale())
            .timezone(entity.getTimezone())
            .deleted(entity.getDeleted())
            .build();
    }

    private UserRiskProfile toDomain(UserRiskProfileEntity entity) {
        return UserRiskProfile.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .kycStatus(KycStatus.fromCode(entity.getKycStatus()))
            .riskLevel(entity.getRiskLevel())
            .assessmentVersion(entity.getAssessmentVersion())
            .assessedAt(entity.getAssessedAt())
            .kycReviewedAt(entity.getKycReviewedAt())
            .deleted(entity.getDeleted())
            .build();
    }

    private UserRole toDomain(UserRoleEntity entity) {
        return UserRole.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .roleCode(entity.getRoleCode())
            .scopeCode(entity.getScopeCode())
            .effectiveFrom(entity.getEffectiveFrom())
            .effectiveTo(entity.getEffectiveTo())
            .deleted(entity.getDeleted())
            .build();
    }

    private UserPreference toDomain(UserPreferenceEntity entity) {
        return UserPreference.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .key(entity.getPreferenceKey())
            .valueType(entity.getValueType())
            .jsonValue(entity.getPreferenceValue())
            .updatedAt(entity.getUpdatedAt())
            .deleted(entity.getDeleted())
            .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
