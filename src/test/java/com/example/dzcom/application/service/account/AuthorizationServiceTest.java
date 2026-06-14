package com.example.dzcom.application.service.account;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.domain.model.account.UserRole;
import com.example.dzcom.domain.repository.account.RolePermissionStore;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 账户授权服务测试。 */
class AuthorizationServiceTest {

    /** 验证多角色权限能够合并为不可变授权快照。 */
    @Test
    void shouldResolveRolesAndPermissions() {
        UserRoleStore userRoles = mock(UserRoleStore.class);
        RolePermissionStore permissions = mock(RolePermissionStore.class);
        CurrentOperatorProvider operators = mock(CurrentOperatorProvider.class);
        when(userRoles.findByUserBizId("user-1")).thenReturn(List.of(
            UserRole.builder().roleCode("ADMIN").build(),
            UserRole.builder().roleCode("RISK").build()
        ));
        when(permissions.findPermissionCodesByRoleCodes(Set.of("ADMIN", "RISK")))
            .thenReturn(Set.of(
                PermissionCodes.ACCOUNT_USER_READ,
                PermissionCodes.ACCOUNT_USER_UPDATE_RISK
            ));

        AuthorizationService service = new AuthorizationService(userRoles, permissions, operators);
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
        UserRoleStore userRoles = mock(UserRoleStore.class);
        RolePermissionStore permissions = mock(RolePermissionStore.class);
        CurrentOperatorProvider operators = mock(CurrentOperatorProvider.class);
        when(operators.required()).thenReturn(new CurrentOperator(
            "user-1",
            "token",
            Set.of("ADMIN"),
            Set.of(PermissionCodes.ACCOUNT_USER_READ)
        ));
        AuthorizationService service = new AuthorizationService(userRoles, permissions, operators);

        assertThrows(BusinessException.class,
            () -> service.require(PermissionCodes.ACCOUNT_ROLE_MANAGE));
    }
}
