package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.model.account.RolePermission;
import com.example.dzcom.domain.repository.account.RolePermissionStore;
import com.example.dzcom.infrastructure.persistence.entity.account.RolePermissionEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** 角色权限映射仓储实现，负责权限映射读写和领域转换。 */
@Repository
@RequiredArgsConstructor
public class RolePermissionStoreImpl implements RolePermissionStore {
    /** MyBatis 角色权限执行器。 */
    private final RolePermissionMapper mapper;

    /** 保存角色权限映射。 */
    @Override
    public RolePermission save(RolePermission value) {
        RolePermissionEntity existing = mapper.selectOne(value.roleCode(), value.permissionCode());
        RolePermissionEntity entity = Optional.ofNullable(existing)
            .map(RolePermissionEntity::toBuilder)
            .orElseGet(RolePermissionEntity::builder)
            .bizId(existing == null ? value.bizId() : existing.getBizId())
            .roleCode(value.roleCode())
            .permissionCode(value.permissionCode())
            .createdAt(value.createdAt())
            .createdBy(value.createdBy())
            .deleted(value.deleted())
            .deletedAt(value.deletedAt())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /** 查询角色全部有效权限映射。 */
    @Override
    public List<RolePermission> findByRoleCode(String roleCode) {
        return mapper.selectByRoleCode(roleCode).stream().map(this::toDomain).toList();
    }

    /** 查询多个角色合并后的有效权限编码。 */
    @Override
    public Set<String> findPermissionCodesByRoleCodes(Set<String> roleCodes) {
        return roleCodes.isEmpty() ? Set.of()
            : Set.copyOf(mapper.selectPermissionCodesByRoleCodes(roleCodes));
    }

    /** 软删除角色的全部权限映射。 */
    @Override
    public void softDeleteByRoleCode(String roleCode) {
        mapper.softDeleteByRoleCode(roleCode);
    }

    /** 将持久化实体转换为角色权限领域对象。 */
    private RolePermission toDomain(RolePermissionEntity entity) {
        return RolePermission.builder()
            .bizId(entity.getBizId())
            .roleCode(entity.getRoleCode())
            .permissionCode(entity.getPermissionCode())
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .deleted(entity.getDeleted())
            .deletedAt(entity.getDeletedAt())
            .build();
    }
}
