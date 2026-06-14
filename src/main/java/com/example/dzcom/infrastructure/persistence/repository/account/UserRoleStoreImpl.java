package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.model.account.UserRole;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserRoleEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户角色仓储实现，直接维护用户角色分配数据。
 */
@Repository
@RequiredArgsConstructor
public class UserRoleStoreImpl implements UserRoleStore {
    /** MyBatis 用户角色执行器。 */
    private final UserRoleMapper mapper;

    /**
     * 保存用户角色。
     *
     * @param value 用户角色领域对象
     * @return 保存后的用户角色
     */
    @Override
    public UserRole save(UserRole value) {
        UserRoleEntity existing = mapper.selectById(value.bizId());
        LocalDateTime createdAt = Optional.ofNullable(existing)
            .map(UserRoleEntity::getCreatedAt)
            .orElse(value.effectiveFrom());
        UserRoleEntity entity = Optional.ofNullable(existing)
            .map(UserRoleEntity::toBuilder)
            .orElseGet(UserRoleEntity::builder)
            .bizId(value.bizId())
            .userBizId(value.userBizId())
            .roleCode(value.roleCode())
            .scopeCode(value.scopeCode())
            .effectiveFrom(value.effectiveFrom())
            .effectiveTo(value.effectiveTo())
            .createdAt(createdAt)
            .createdBy(value.createdBy())
            .deleted(value.deleted())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 查询用户全部未删除角色。
     *
     * @param userBizId 用户业务标识
     * @return 用户角色列表
     */
    @Override
    public List<UserRole> findByUserBizId(String userBizId) {
        return findEntitiesByUserBizId(userBizId, false).stream()
            .map(this::toDomain)
            .toList();
    }

    /** 查询用户指定角色分配，包含已撤销记录。 */
    @Override
    public Optional<UserRole> findByUserBizIdAndRoleCode(String userBizId, String roleCode) {
        return Optional.ofNullable(mapper.selectByUserBizIdAndRoleCode(userBizId, roleCode))
            .map(this::toDomain);
    }

    /** 查询持有指定角色的用户业务标识。 */
    @Override
    public List<String> findUserBizIdsByRoleCode(String roleCode) {
        return mapper.selectUserBizIdsByRoleCode(roleCode);
    }

    /** 统计通过其他角色具备指定权限的有效用户数量。 */
    @Override
    public long countUsersWithPermissionExcludingRole(String permissionCode, String excludedRoleCode) {
        return mapper.countUsersWithPermissionExcludingRole(permissionCode, excludedRoleCode);
    }

    /** 软删除用户的指定角色。 */
    @Override
    public void softDelete(String userBizId, String roleCode) {
        mapper.softDelete(userBizId, roleCode);
    }

    /**
     * 软删除用户全部角色。
     *
     * @param userBizId 用户业务标识
     */
    @Override
    public void softDeleteByUserBizId(String userBizId) {
        mapper.softDeleteByUserBizId(userBizId);
    }

    /**
     * 查询用户角色实体。
     *
     * @param userBizId 用户业务标识
     * @param includeDeleted 是否包含已删除数据
     * @return 用户角色实体列表
     */
    private List<UserRoleEntity> findEntitiesByUserBizId(String userBizId, boolean includeDeleted) {
        return mapper.selectByUserBizId(userBizId, includeDeleted);
    }

    /**
     * 将角色实体转换为领域对象。
     *
     * @param entity 用户角色实体
     * @return 用户角色领域对象
     */
    private UserRole toDomain(UserRoleEntity entity) {
        return UserRole.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .roleCode(entity.getRoleCode())
            .scopeCode(entity.getScopeCode())
            .effectiveFrom(entity.getEffectiveFrom())
            .effectiveTo(entity.getEffectiveTo())
            .createdBy(entity.getCreatedBy())
            .deleted(entity.getDeleted())
            .build();
    }
}
