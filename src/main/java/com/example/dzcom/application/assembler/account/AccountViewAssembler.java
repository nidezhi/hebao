package com.example.dzcom.application.assembler.account;

import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.*;
import com.example.dzcom.domain.repository.account.AccountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将账户域多表数据组装为接口视图。
 *
 * <p>查询服务只调用本组装器，不向 Controller 暴露领域对象或 JPA Entity。</p>
 */
@Component
@RequiredArgsConstructor
public class AccountViewAssembler {
    private final AccountStore store;

    public UserView assemble(User user) {
        List<LoginIdentity> identities = store.findIdentities(user.getBizId());
        UserProfile profile = store.findProfile(user.getBizId()).orElse(null);
        UserRiskProfile risk = store.findRiskProfile(user.getBizId()).orElse(null);
        Set<String> roles = store.findRoles(user.getBizId()).stream()
            .map(UserRole::roleCode)
            .collect(Collectors.toUnmodifiableSet());
        return UserView.builder()
            .bizId(user.getBizId())
            .userNo(user.getUserNo())
            .username(identityValue(identities, IdentityType.USERNAME))
            .email(identityValue(identities, IdentityType.EMAIL))
            .phone(identityValue(identities, IdentityType.PHONE))
            .nickname(profile == null ? null : profile.nickname())
            .avatarUrl(profile == null ? null : profile.avatarUrl())
            .status(user.getStatus())
            .kycStatus(risk == null ? null : risk.kycStatus())
            .riskLevel(risk == null ? 1 : risk.riskLevel())
            .roles(roles)
            .registeredAt(user.getRegisteredAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    private String identityValue(List<LoginIdentity> identities, IdentityType type) {
        return identities.stream().filter(identity -> identity.type() == type)
            .map(LoginIdentity::value).findFirst().orElse(null);
    }
}
