package com.example.dzcom.application.service.account;

import com.example.dzcom.application.assembler.account.AccountViewAssembler;
import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.service.ClockProvider;
import com.example.dzcom.common.service.IdGenerator;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.domain.model.account.*;
import com.example.dzcom.domain.repository.account.LoginIdentityStore;
import com.example.dzcom.domain.repository.account.UserCredentialStore;
import com.example.dzcom.domain.repository.account.UserProfileStore;
import com.example.dzcom.domain.repository.account.UserRiskProfileStore;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import com.example.dzcom.domain.repository.account.UserStore;
import com.example.dzcom.domain.service.account.IdentityNormalizer;
import com.example.dzcom.domain.service.account.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 账户注册应用服务。
 *
 * <p>在单个本地事务中显式创建用户主体、登录标识、密码凭据、资料、
 * 风险画像和默认角色，不依赖 JPA 级联或数据库外键。</p>
 */
@Service
@RequiredArgsConstructor
public class AccountRegistrationService {
    private final UserStore users;
    private final LoginIdentityStore identities;
    private final UserCredentialStore credentials;
    private final UserProfileStore profiles;
    private final UserRiskProfileStore riskProfiles;
    private final UserRoleStore roles;
    private final IdGenerator idGenerator;
    private final ClockProvider clock;
    private final IdentityNormalizer normalizer;
    private final PasswordHasher passwordHasher;
    private final AccountViewAssembler assembler;

    /**
     * 创建或保存对应的业务数据。
     *
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public UserView register(RegisterCommand command) {
        validatePassword(command.password());
        LocalDateTime now = clock.now();
        List<LoginIdentity> identities = buildIdentities(command, now);
        identities.forEach(this::ensureIdentityAvailable);
        User user = User.register(idGenerator.newBizId(), idGenerator.newUserNo(), now);
        try {
            users.save(user);
            identities.stream()
                .map(identity -> identity.toBuilder().userBizId(user.getBizId()).build())
                .forEach(this.identities::save);
            credentials.save(UserCredential.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .secretHash(passwordHasher.hash(command.password()))
                .hashAlgorithm("BCRYPT")
                .credentialVersion(1)
                .failedAttempts(0)
                .changedAt(now)
                .deleted(0)
                .build());
            profiles.save(UserProfile.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .nickname(command.nickname())
                .locale("zh-CN")
                .timezone("Asia/Shanghai")
                .deleted(0)
                .build());
            riskProfiles.save(UserRiskProfile.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .kycStatus(KycStatus.UNVERIFIED)
                .riskLevel(1)
                .deleted(0)
                .build());
            roles.save(UserRole.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .roleCode(command.initialRole() == null ? "USER" : command.initialRole())
                .scopeCode("GLOBAL")
                .effectiveFrom(now)
                .deleted(0)
                .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名、邮箱或手机号已存在");
        }
        return assembler.assemble(user);
    }

    /**
     * 执行 build identities 处理。
     *
     * @param command 应用用例命令
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private List<LoginIdentity> buildIdentities(RegisterCommand command, LocalDateTime now) {
        return Stream.of(
                buildIdentity(IdentityType.USERNAME, command.username(), true, now),
                buildIdentity(IdentityType.EMAIL, command.email(), false, now),
                buildIdentity(IdentityType.PHONE, command.phone(), false, now)
            )
            .flatMap(java.util.Optional::stream)
            .toList();
    }

    /**
     * 执行 build identity 处理。
     *
     * @param type 数据类型
     * @param value 待处理的数据值
     * @param required required 参数
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private java.util.Optional<LoginIdentity> buildIdentity(IdentityType type, String value,
                                                            boolean required, LocalDateTime now) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, type + " 不能为空");
            }
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(LoginIdentity.builder()
            .bizId(idGenerator.newBizId())
            .type(type)
            .value(value.trim())
            .normalizedValue(normalizer.normalize(type, value))
            .verified(type == IdentityType.USERNAME)
            .active(true)
            .createdAt(now)
            .deleted(0)
            .build());
    }

    /**
     * 执行 ensure identity available 处理。
     *
     * @param identity identity 参数
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void ensureIdentityAvailable(LoginIdentity identity) {
        if (identities.findByTypeAndNormalizedValue(identity.type(), identity.normalizedValue()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名、邮箱或手机号已存在");
        }
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param password password 参数
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8
            || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码至少 8 位且必须包含字母和数字");
        }
    }
}
