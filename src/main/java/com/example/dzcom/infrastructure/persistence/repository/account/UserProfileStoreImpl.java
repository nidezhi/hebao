package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.model.account.UserProfile;
import com.example.dzcom.domain.repository.account.UserProfileStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserProfileEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户资料仓储实现，直接维护用户资料表。
 */
@Repository
@RequiredArgsConstructor
public class UserProfileStoreImpl implements UserProfileStore {
    /** MyBatis 用户资料执行器。 */
    private final UserProfileMapper mapper;

    /**
     * 保存用户资料。
     *
     * @param value 用户资料领域对象
     * @return 保存后的用户资料
     */
    @Override
    public UserProfile save(UserProfile value) {
        UserProfileEntity existing = mapper.selectById(value.bizId());
        LocalDateTime now = LocalDateTime.now();
        UserProfileEntity entity = Optional.ofNullable(existing)
            .map(UserProfileEntity::toBuilder)
            .orElseGet(UserProfileEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .nickname(value.nickname())
            .avatarUrl(value.avatarUrl())
            .locale(value.locale())
            .timezone(value.timezone())
            .createdAt(Optional.ofNullable(existing).map(UserProfileEntity::getCreatedAt).orElse(now))
            .updatedAt(now)
            .deleted(value.deleted())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 查询用户未删除资料。
     *
     * @param userBizId 用户业务标识
     * @return 用户资料
     */
    @Override
    public Optional<UserProfile> findByUserBizId(String userBizId) {
        return findEntityByUserBizId(userBizId, false).map(this::toDomain);
    }

    /**
     * 软删除用户资料。
     *
     * @param userBizId 用户业务标识
     */
    @Override
    public void softDeleteByUserBizId(String userBizId) {
        mapper.softDeleteByUserBizId(userBizId);
    }

    /**
     * 查询用户资料实体。
     *
     * @param userBizId 用户业务标识
     * @param includeDeleted 是否包含已删除数据
     * @return 用户资料实体
     */
    private Optional<UserProfileEntity> findEntityByUserBizId(String userBizId, boolean includeDeleted) {
        return Optional.ofNullable(mapper.selectByUserBizId(userBizId, includeDeleted));
    }

    /**
     * 将资料实体转换为领域对象。
     *
     * @param entity 用户资料实体
     * @return 用户资料领域对象
     */
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
}
