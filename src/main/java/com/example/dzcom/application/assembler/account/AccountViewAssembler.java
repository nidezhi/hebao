package com.example.dzcom.application.assembler.account;

import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.AuthorizationService;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.*;
import com.example.dzcom.domain.repository.account.LoginIdentityStore;
import com.example.dzcom.domain.repository.account.UserProfileStore;
import com.example.dzcom.domain.repository.account.UserRiskProfileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 将账户域多表数据组装为接口视图。
 *
 * <p>查询服务只调用本组装器，不向 Controller 暴露领域对象或持久化 Entity。</p>
 */
@Component
@RequiredArgsConstructor
public class AccountViewAssembler {
    private final LoginIdentityStore identities;
    private final UserProfileStore profiles;
    private final UserRiskProfileStore riskProfiles;
    private final AuthorizationService authorization;

    /**
     * 将源对象转换为目标视图或领域对象。
     *
     * @param user user 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    public UserView assemble(User user) {
        List<LoginIdentity> loginIdentities = identities.findByUserBizId(user.getBizId());
        UserProfile profile = profiles.findByUserBizId(user.getBizId()).orElse(null);
        UserRiskProfile risk = riskProfiles.findByUserBizId(user.getBizId()).orElse(null);
        AuthorizationService.AuthorizationSnapshot snapshot = authorization.resolve(user.getBizId());
        return UserView.builder()
            .bizId(user.getBizId())
            .userNo(user.getUserNo())
            .username(identityValue(loginIdentities, IdentityType.USERNAME))
            .email(identityValue(loginIdentities, IdentityType.EMAIL))
            .phone(identityValue(loginIdentities, IdentityType.PHONE))
            .nickname(profile == null ? null : profile.nickname())
            .avatarUrl(profile == null ? null : profile.avatarUrl())
            .status(user.getStatus())
            .kycStatus(risk == null ? null : risk.kycStatus())
            .riskLevel(risk == null ? 1 : risk.riskLevel())
            .roles(snapshot.roleCodes())
            .permissions(snapshot.permissionCodes())
            .registeredAt(user.getRegisteredAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    /**
     * 执行 identity value 处理。
     *
     * @param identities identities 参数
     * @param type 数据类型
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private String identityValue(List<LoginIdentity> identities, IdentityType type) {
        return identities.stream().filter(identity -> identity.type() == type)
            .map(LoginIdentity::value).findFirst().orElse(null);
    }
}
