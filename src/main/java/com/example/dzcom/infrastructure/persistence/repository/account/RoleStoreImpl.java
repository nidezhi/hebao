package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.domain.model.account.Role;
import com.example.dzcom.domain.repository.account.RoleStore;
import com.example.dzcom.infrastructure.persistence.entity.account.RoleEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 角色定义仓储实现，负责角色实体读写和领域转换。 */
@Repository
@RequiredArgsConstructor
public class RoleStoreImpl implements RoleStore {
    /** MyBatis 角色定义执行器。 */
    private final RoleMapper mapper;

    /** 保存角色定义。 */
    @Override
    public Role save(Role value) {
        RoleEntity entity = RoleEntity.builder()
            .bizId(value.bizId())
            .roleCode(value.roleCode())
            .roleName(value.roleName())
            .description(value.description())
            .roleType(value.roleType())
            .status(value.status())
            .version(value.version())
            .createdAt(value.createdAt())
            .updatedAt(value.updatedAt())
            .createdBy(value.createdBy())
            .updatedBy(value.updatedBy())
            .deleted(value.deleted())
            .deletedAt(value.deletedAt())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /** 根据角色编码查询未删除角色。 */
    @Override
    public Optional<Role> findByCode(String roleCode) {
        return Optional.ofNullable(mapper.selectByCode(roleCode)).map(this::toDomain);
    }

    /** 查询全部未删除角色。 */
    @Override
    public List<Role> findAll() {
        return mapper.selectAll().stream().map(this::toDomain).toList();
    }

    /** 将持久化实体转换为角色领域对象。 */
    private Role toDomain(RoleEntity entity) {
        return Role.builder()
            .bizId(entity.getBizId())
            .roleCode(entity.getRoleCode())
            .roleName(entity.getRoleName())
            .description(entity.getDescription())
            .roleType(entity.getRoleType())
            .status(entity.getStatus())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .deleted(entity.getDeleted())
            .deletedAt(entity.getDeletedAt())
            .build();
    }
}
