package com.example.dzcom.application.service.account;

import java.util.Set;

public record CurrentOperator(String userBizId, String sessionToken, Set<String> roles) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
