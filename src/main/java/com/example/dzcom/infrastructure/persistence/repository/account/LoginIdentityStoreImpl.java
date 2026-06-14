package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.LoginIdentity;
import com.example.dzcom.domain.repository.account.LoginIdentityStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserIdentityEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.LoginIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 登录标识仓储实现，直接负责登录标识表的查询、保存、软删除和领域对象转换。
 */
@Repository
@RequiredArgsConstructor
public class LoginIdentityStoreImpl implements LoginIdentityStore {
    /** MyBatis 登录标识执行器。 */
    private final LoginIdentityMapper mapper;

    /**
     * 保存登录标识。
     *
     * @param value 登录标识领域对象
     * @return 保存后的登录标识
     */
    @Override
    public LoginIdentity save(LoginIdentity value) {
        UserIdentityEntity existing = mapper.selectById(value.bizId());
        UserIdentityEntity entity = Optional.ofNullable(existing)
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
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 根据标识类型和标准化值查询有效登录标识。
     *
     * @param type 标识类型
     * @param normalizedValue 标准化标识值
     * @return 匹配的登录标识
     */
    @Override
    public Optional<LoginIdentity> findByTypeAndNormalizedValue(IdentityType type, String normalizedValue) {
        return Optional.ofNullable(mapper.selectByTypeAndNormalizedValue(type.name(), normalizedValue))
            .map(this::toDomain);
    }

    /**
     * 根据用户和标识类型查询未删除登录标识。
     *
     * @param userBizId 用户业务标识
     * @param type 标识类型
     * @return 匹配的登录标识
     */
    @Override
    public Optional<LoginIdentity> findByUserBizIdAndType(String userBizId, IdentityType type) {
        return Optional.ofNullable(mapper.selectByUserBizIdAndType(userBizId, type.name()))
            .map(this::toDomain);
    }

    /**
     * 查询用户全部未删除登录标识。
     *
     * @param userBizId 用户业务标识
     * @return 登录标识列表
     */
    @Override
    public List<LoginIdentity> findByUserBizId(String userBizId) {
        return findEntitiesByUserBizId(userBizId, false).stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 软删除用户全部登录标识。
     *
     * @param userBizId 用户业务标识
     */
    @Override
    public void softDeleteByUserBizId(String userBizId) {
        mapper.softDeleteByUserBizId(userBizId);
    }

    /**
     * 查询用户登录标识实体。
     *
     * @param userBizId 用户业务标识
     * @param includeDeleted 是否包含已删除数据
     * @return 登录标识实体列表
     */
    private List<UserIdentityEntity> findEntitiesByUserBizId(String userBizId, boolean includeDeleted) {
        return mapper.selectByUserBizId(userBizId, includeDeleted);
    }

    /**
     * 将登录标识实体转换为领域对象。
     *
     * @param entity 登录标识实体
     * @return 登录标识领域对象
     */
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
}
