package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.domain.model.account.UserRiskProfile;
import com.example.dzcom.domain.repository.account.UserRiskProfileStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserRiskProfileEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户风险画像仓储实现，直接维护 KYC 和风险等级数据。
 */
@Repository
@RequiredArgsConstructor
public class UserRiskProfileStoreImpl implements UserRiskProfileStore {
    /** JPA 实体管理器。 */
    private final EntityManager entityManager;

    /**
     * 保存用户风险画像。
     *
     * @param value 风险画像领域对象
     * @return 保存后的风险画像
     */
    @Override
    public UserRiskProfile save(UserRiskProfile value) {
        UserRiskProfileEntity existing = entityManager.find(UserRiskProfileEntity.class, value.bizId());
        LocalDateTime now = LocalDateTime.now();
        UserRiskProfileEntity entity = Optional.ofNullable(existing)
            .map(UserRiskProfileEntity::toBuilder)
            .orElseGet(UserRiskProfileEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .kycStatus(value.kycStatus().code())
            .riskLevel(value.riskLevel())
            .assessmentVersion(value.assessmentVersion())
            .assessedAt(value.assessedAt())
            .kycReviewedAt(value.kycReviewedAt())
            .createdAt(Optional.ofNullable(existing).map(UserRiskProfileEntity::getCreatedAt).orElse(now))
            .updatedAt(now)
            .deleted(value.deleted())
            .build();
        return toDomain(entityManager.merge(entity));
    }

    /**
     * 查询用户未删除风险画像。
     *
     * @param userBizId 用户业务标识
     * @return 风险画像
     */
    @Override
    public Optional<UserRiskProfile> findByUserBizId(String userBizId) {
        return findEntityByUserBizId(userBizId, false).map(this::toDomain);
    }

    /**
     * 软删除用户风险画像。
     *
     * @param userBizId 用户业务标识
     */
    @Override
    public void softDeleteByUserBizId(String userBizId) {
        findEntityByUserBizId(userBizId, true)
            .map(UserRiskProfileEntity::toBuilder)
            .map(builder -> builder.deleted(1).build())
            .ifPresent(entityManager::merge);
    }

    /**
     * 查询用户风险画像实体。
     *
     * @param userBizId 用户业务标识
     * @param includeDeleted 是否包含已删除数据
     * @return 风险画像实体
     */
    private Optional<UserRiskProfileEntity> findEntityByUserBizId(String userBizId, boolean includeDeleted) {
        String jpql = includeDeleted
            ? "select r from UserRiskProfileEntity r where r.userBizId = :userBizId"
            : "select r from UserRiskProfileEntity r where r.userBizId = :userBizId and r.deleted = 0";
        return entityManager.createQuery(jpql, UserRiskProfileEntity.class)
            .setParameter("userBizId", userBizId)
            .getResultStream()
            .findFirst();
    }

    /**
     * 将风险画像实体转换为领域对象。
     *
     * @param entity 风险画像实体
     * @return 风险画像领域对象
     */
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
}
