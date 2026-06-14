package com.example.dzcom.application.service.account;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.domain.model.account.UserRole;
import com.example.dzcom.domain.model.account.RolePermission;
import com.example.dzcom.domain.repository.account.RolePermissionStore;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 账户授权服务测试。 */
class AuthorizationServiceTest {

    /** 验证多角色权限能够合并为不可变授权快照。 */
    @Test
    void shouldResolveRolesAndPermissions() {
        List<UserRole> roles = List.of(
            UserRole.builder().roleCode("ADMIN").build(),
            UserRole.builder().roleCode("RISK").build()
        );
        Set<String> permissionCodes = Set.of(
            PermissionCodes.ACCOUNT_USER_READ,
            PermissionCodes.ACCOUNT_USER_UPDATE_RISK
        );

        AuthorizationService service = new AuthorizationService(
            new StubUserRoleStore(roles),
            new StubRolePermissionStore(permissionCodes),
            () -> null
        );
        AuthorizationService.AuthorizationSnapshot snapshot = service.resolve("user-1");

        assertEquals(Set.of("ADMIN", "RISK"), snapshot.roleCodes());
        assertEquals(Set.of(
            PermissionCodes.ACCOUNT_USER_READ,
            PermissionCodes.ACCOUNT_USER_UPDATE_RISK
        ), snapshot.permissionCodes());
    }

    /** 验证缺少权限时统一抛出禁止访问业务异常。 */
    @Test
    void shouldRejectOperatorWithoutPermission() {
        CurrentOperator operator = new CurrentOperator(
            "user-1",
            "token",
            Set.of("ADMIN"),
            Set.of(PermissionCodes.ACCOUNT_USER_READ)
        );
        AuthorizationService service = new AuthorizationService(
            new StubUserRoleStore(List.of()),
            new StubRolePermissionStore(Set.of()),
            () -> operator
        );

        assertThrows(BusinessException.class,
            () -> service.require(PermissionCodes.ACCOUNT_ROLE_MANAGE));
    }

    /** 仅支持授权快照查询的用户角色仓储桩。 */
    private record StubUserRoleStore(List<UserRole> roles) implements UserRoleStore {
        @Override
        public UserRole save(UserRole role) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UserRole> findByUserBizId(String userBizId) {
            return roles;
        }

        @Override
        public Optional<UserRole> findByUserBizIdAndRoleCode(String userBizId, String roleCode) {
            return Optional.empty();
        }

        @Override
        public List<String> findUserBizIdsByRoleCode(String roleCode) {
            return List.of();
        }

        @Override
        public long countUsersWithPermissionExcludingRole(String permissionCode,
                                                          String excludedRoleCode) {
            return 0;
        }

        @Override
        public long countUsersWithPermissionExcludingAssignment(String permissionCode,
                                                                String userBizId,
                                                                String roleCode) {
            return 0;
        }

        @Override
        public void softDelete(String userBizId, String roleCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void softDeleteByUserBizId(String userBizId) {
            throw new UnsupportedOperationException();
        }
    }

    /** 仅支持权限编码查询的角色权限仓储桩。 */
    private record StubRolePermissionStore(Set<String> permissionCodes)
        implements RolePermissionStore {
        @Override
        public RolePermission save(RolePermission permission) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RolePermission> findByRoleCode(String roleCode) {
            return List.of();
        }

        @Override
        public Set<String> findPermissionCodesByRoleCodes(Set<String> roleCodes) {
            return permissionCodes;
        }

        @Override
        public void softDeleteByRoleCode(String roleCode) {
            throw new UnsupportedOperationException();
        }
    }
}
