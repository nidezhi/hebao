package com.example.dzcom.application.service.account;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.account.RoleView;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.domain.model.account.Role;
import com.example.dzcom.domain.model.account.RolePermission;
import com.example.dzcom.domain.model.account.UserRole;
import com.example.dzcom.domain.repository.account.RolePermissionStore;
import com.example.dzcom.domain.repository.account.RoleStore;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import com.example.dzcom.domain.repository.account.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** 角色、角色权限和用户角色分配的应用服务。 */
@Service
@RequiredArgsConstructor
public class RoleApplicationService {
    private final RoleStore roles;
    private final RolePermissionStore permissions;
    private final UserRoleStore userRoles;
    private final UserStore users;
    private final AuthorizationService authorization;
    private final SessionService sessions;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final com.example.dzcom.application.assembler.account.AccountViewAssembler accountAssembler;

    /** 查询全部角色及其有效权限。 */
    @Transactional(readOnly = true)
    public List<RoleView> list() {
        authorization.require(PermissionCodes.ACCOUNT_ROLE_READ);
        return roles.findAll().stream().map(this::toView).toList();
    }

    /** 查询权限目录，用于前端结构化权限选择器。 */
    @Transactional(readOnly = true)
    public List<PermissionCodes.PermissionDescriptor> permissionCatalog() {
        authorization.require(PermissionCodes.ACCOUNT_ROLE_READ);
        return PermissionCodes.catalog();
    }

    /** 创建自定义角色。 */
    @Transactional
    public RoleView create(String roleCode, String roleName, String description) {
        CurrentOperator operator = authorization.require(PermissionCodes.ACCOUNT_ROLE_MANAGE);
        String normalizedCode = normalizeCode(roleCode);
        if (roles.findByCode(normalizedCode).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "角色编码已存在");
        }
        LocalDateTime now = clock.now();
        Role role = Role.builder()
            .bizId(ids.newBizId())
            .roleCode(normalizedCode)
            .roleName(roleName.trim())
            .description(trimToNull(description))
            .roleType("CUSTOM")
            .status(1)
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(operator.userBizId())
            .updatedBy(operator.userBizId())
            .deleted(0)
            .build();
        return toView(roles.save(role));
    }

    /** 更新角色名称和说明。 */
    @Transactional
    public RoleView update(String roleCode, String roleName, String description) {
        CurrentOperator operator = authorization.require(PermissionCodes.ACCOUNT_ROLE_MANAGE);
        Role role = requiredRole(roleCode);
        Role updated = role.toBuilder()
            .roleName(roleName.trim())
            .description(trimToNull(description))
            .version(role.version() + 1)
            .updatedAt(clock.now())
            .updatedBy(operator.userBizId())
            .build();
        return toView(roles.save(updated));
    }

    /** 启用或停用角色，并撤销受影响用户会话。 */
    @Transactional
    public RoleView changeStatus(String roleCode, boolean enabled) {
        CurrentOperator operator = authorization.require(PermissionCodes.ACCOUNT_ROLE_MANAGE);
        Role role = requiredRole(roleCode);
        if (!enabled) {
            ensureRoleManagerRemains(role.roleCode());
        }
        Role updated = role.toBuilder()
            .status(enabled ? 1 : 0)
            .version(role.version() + 1)
            .updatedAt(clock.now())
            .updatedBy(operator.userBizId())
            .build();
        Role saved = roles.save(updated);
        revokeRoleSessions(saved.roleCode());
        return toView(saved);
    }

    /** 使用请求集合覆盖角色权限，并撤销受影响用户会话。 */
    @Transactional
    public RoleView configurePermissions(String roleCode, Set<String> permissionCodes) {
        CurrentOperator operator = authorization.require(PermissionCodes.ACCOUNT_ROLE_MANAGE);
        Role role = requiredRole(roleCode);
        Set<String> normalized = permissionCodes.stream()
            .map(String::trim)
            .collect(Collectors.toUnmodifiableSet());
        normalized.stream()
            .filter(permission -> !PermissionCodes.contains(permission))
            .findFirst()
            .ifPresent(permission -> {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "未注册的权限编码: " + permission);
            });
        if (!normalized.contains(PermissionCodes.ACCOUNT_ROLE_MANAGE)) {
            ensureRoleManagerRemains(role.roleCode());
        }
        permissions.softDeleteByRoleCode(role.roleCode());
        LocalDateTime now = clock.now();
        normalized.stream()
            .map(permission -> RolePermission.builder()
                .bizId(ids.newBizId())
                .roleCode(role.roleCode())
                .permissionCode(permission)
                .createdAt(now)
                .createdBy(operator.userBizId())
                .deleted(0)
                .build())
            .forEach(permissions::save);
        revokeRoleSessions(role.roleCode());
        return toView(role);
    }

    /** 给用户分配有效角色，并撤销该用户旧会话。 */
    @Transactional
    public UserView assign(String userBizId, String roleCode, LocalDateTime effectiveTo) {
        CurrentOperator operator = authorization.require(PermissionCodes.ACCOUNT_ROLE_ASSIGN);
        com.example.dzcom.domain.model.account.User user = users.findByBizId(userBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        Role role = requiredRole(roleCode);
        if (!role.active()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "角色未启用");
        }
        LocalDateTime now = clock.now();
        if (effectiveTo != null && !effectiveTo.isAfter(now)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "角色失效时间必须晚于当前时间");
        }
        UserRole existing = userRoles.findByUserBizIdAndRoleCode(userBizId, role.roleCode()).orElse(null);
        userRoles.save(UserRole.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .userBizId(userBizId)
            .roleCode(role.roleCode())
            .scopeCode("GLOBAL")
            .effectiveFrom(now)
            .effectiveTo(effectiveTo)
            .createdBy(operator.userBizId())
            .deleted(0)
            .build());
        sessions.revokeAll(userBizId);
        return accountAssembler.assemble(user);
    }

    /** 撤销用户指定角色，并撤销该用户旧会话。 */
    @Transactional
    public UserView revoke(String userBizId, String roleCode) {
        authorization.require(PermissionCodes.ACCOUNT_ROLE_ASSIGN);
        com.example.dzcom.domain.model.account.User user = users.findByBizId(userBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        String normalizedRole = normalizeCode(roleCode);
        if (roleHasPermission(normalizedRole, PermissionCodes.ACCOUNT_ROLE_MANAGE)
            && userRoles.countUsersWithPermissionExcludingAssignment(
                PermissionCodes.ACCOUNT_ROLE_MANAGE, userBizId, normalizedRole) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "必须保留至少一个角色管理员");
        }
        userRoles.softDelete(userBizId, normalizedRole);
        sessions.revokeAll(userBizId);
        return accountAssembler.assemble(user);
    }

    /** 获取必须存在的角色定义。 */
    private Role requiredRole(String roleCode) {
        return roles.findByCode(normalizeCode(roleCode))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "角色不存在"));
    }

    /** 将角色定义和权限集合转换为接口视图。 */
    private RoleView toView(Role role) {
        Set<String> permissionCodes = permissions.findByRoleCode(role.roleCode()).stream()
            .map(RolePermission::permissionCode)
            .collect(Collectors.toUnmodifiableSet());
        return RoleView.builder()
            .roleCode(role.roleCode())
            .roleName(role.roleName())
            .description(role.description())
            .roleType(role.roleType())
            .enabled(role.status() == 1)
            .permissions(permissionCodes)
            .createdAt(role.createdAt())
            .updatedAt(role.updatedAt())
            .build();
    }

    /** 撤销持有指定角色用户的全部会话。 */
    private void revokeRoleSessions(String roleCode) {
        userRoles.findUserBizIdsByRoleCode(roleCode).stream()
            .forEach(sessions::revokeAll);
    }

    /** 确保移除指定角色后仍有其他有效用户具备角色管理权限。 */
    private void ensureRoleManagerRemains(String roleCode) {
        if (roleHasPermission(roleCode, PermissionCodes.ACCOUNT_ROLE_MANAGE)
            && userRoles.countUsersWithPermissionExcludingRole(
                PermissionCodes.ACCOUNT_ROLE_MANAGE, roleCode) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "必须保留至少一个角色管理员");
        }
    }

    /** 判断角色当前是否具有指定权限。 */
    private boolean roleHasPermission(String roleCode, String permissionCode) {
        return permissions.findByRoleCode(roleCode).stream()
            .anyMatch(permission -> permissionCode.equals(permission.permissionCode()));
    }

    /** 标准化角色编码。 */
    private String normalizeCode(String roleCode) {
        return roleCode.trim().toUpperCase(Locale.ROOT);
    }

    /** 将空白说明转换为空值。 */
    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
