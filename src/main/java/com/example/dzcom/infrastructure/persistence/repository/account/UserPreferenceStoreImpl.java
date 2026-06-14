package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.model.account.UserPreference;
import com.example.dzcom.domain.repository.account.UserPreferenceStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserPreferenceEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户偏好仓储实现，直接维护用户偏好数据。
 */
@Repository
@RequiredArgsConstructor
public class UserPreferenceStoreImpl implements UserPreferenceStore {
    /** MyBatis 用户偏好执行器。 */
    private final UserPreferenceMapper mapper;

    /**
     * 保存用户偏好。
     *
     * @param value 用户偏好领域对象
     * @return 保存后的用户偏好
     */
    @Override
    public UserPreference save(UserPreference value) {
        UserPreferenceEntity existing = mapper.selectById(value.bizId());
        LocalDateTime createdAt = Optional.ofNullable(existing)
            .map(UserPreferenceEntity::getCreatedAt)
            .orElse(value.updatedAt());
        UserPreferenceEntity entity = Optional.ofNullable(existing)
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
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 根据用户和偏好键查询偏好。
     *
     * @param userBizId 用户业务标识
     * @param key 偏好键
     * @param includeDeleted 是否包含已删除数据
     * @return 用户偏好
     */
    @Override
    public Optional<UserPreference> findByUserBizIdAndKey(String userBizId, String key,
                                                          boolean includeDeleted) {
        return Optional.ofNullable(mapper.selectByUserBizIdAndKey(userBizId, key, includeDeleted))
            .map(this::toDomain);
    }

    /**
     * 查询用户全部未删除偏好。
     *
     * @param userBizId 用户业务标识
     * @return 用户偏好列表
     */
    @Override
    public List<UserPreference> findByUserBizId(String userBizId) {
        return mapper.selectByUserBizId(userBizId)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 软删除用户全部偏好。
     *
     * @param userBizId 用户业务标识
     */
    @Override
    public void softDeleteByUserBizId(String userBizId) {
        mapper.softDeleteByUserBizId(userBizId);
    }

    /**
     * 将偏好实体转换为领域对象。
     *
     * @param entity 用户偏好实体
     * @return 用户偏好领域对象
     */
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
}
