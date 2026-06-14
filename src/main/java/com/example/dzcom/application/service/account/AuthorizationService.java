package com.example.dzcom.application.service.account;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.domain.model.account.UserRole;
import com.example.dzcom.domain.repository.account.RolePermissionStore;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/** 账户授权服务，统一计算有效角色和权限并执行权限校验。 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final UserRoleStore userRoles;
    private final RolePermissionStore rolePermissions;
    private final CurrentOperatorProvider currentOperator;

    /**
     * 计算指定用户当前有效的角色和权限。
     *
     * @param userBizId 用户业务标识
     * @return 不可变授权快照
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public AuthorizationSnapshot resolve(String userBizId) {
        Set<String> roleCodes = userRoles.findByUserBizId(userBizId).stream()
            .map(UserRole::roleCode)
            .collect(Collectors.toUnmodifiableSet());
        return new AuthorizationSnapshot(
            roleCodes,
            rolePermissions.findPermissionCodesByRoleCodes(roleCodes)
        );
    }

    /**
     * 校验当前操作者是否具备指定权限。
     *
     * @param permissionCode 权限编码
     * @return 当前操作者
     * @throws BusinessException 当前操作者权限不足时抛出
     * @author dz
     * @date 2026-06-14
     */
    public CurrentOperator require(String permissionCode) {
        CurrentOperator operator = currentOperator.required();
        if (!operator.hasPermission(permissionCode)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "权限不足");
        }
        return operator;
    }

    /** 用户当前有效角色与权限的不可变快照。 */
    public record AuthorizationSnapshot(Set<String> roleCodes, Set<String> permissionCodes) {
        public AuthorizationSnapshot {
            roleCodes = Set.copyOf(roleCodes);
            permissionCodes = Set.copyOf(permissionCodes);
        }
    }
}
