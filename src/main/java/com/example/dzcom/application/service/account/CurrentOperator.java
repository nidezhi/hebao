package com.example.dzcom.application.service.account;

import java.util.Set;

public record CurrentOperator(String userBizId, String sessionToken, Set<String> roles) {
    /**
     * 执行 has role 处理。
     *
     * @param role role 参数
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
