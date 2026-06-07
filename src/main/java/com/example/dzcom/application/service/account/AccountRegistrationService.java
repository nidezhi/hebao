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
import com.example.dzcom.domain.repository.account.AccountStore;
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
    private final AccountStore store;
    private final IdGenerator idGenerator;
    private final ClockProvider clock;
    private final IdentityNormalizer normalizer;
    private final PasswordHasher passwordHasher;
    private final AccountViewAssembler assembler;

    @Transactional
    public UserView register(RegisterCommand command) {
        validatePassword(command.password());
        LocalDateTime now = clock.now();
        List<LoginIdentity> identities = buildIdentities(command, now);
        identities.forEach(this::ensureIdentityAvailable);
        User user = User.register(idGenerator.newBizId(), idGenerator.newUserNo(), now);
        try {
            store.saveUser(user);
            identities.stream()
                .map(identity -> identity.toBuilder().userBizId(user.getBizId()).build())
                .forEach(store::saveIdentity);
            store.saveCredential(UserCredential.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .secretHash(passwordHasher.hash(command.password()))
                .hashAlgorithm("BCRYPT")
                .credentialVersion(1)
                .failedAttempts(0)
                .changedAt(now)
                .deleted(false)
                .build());
            store.saveProfile(UserProfile.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .nickname(command.nickname())
                .locale("zh-CN")
                .timezone("Asia/Shanghai")
                .deleted(false)
                .build());
            store.saveRiskProfile(UserRiskProfile.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .kycStatus(KycStatus.UNVERIFIED)
                .riskLevel(1)
                .deleted(false)
                .build());
            store.saveRole(UserRole.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(user.getBizId())
                .roleCode(command.initialRole() == null ? "USER" : command.initialRole())
                .scopeCode("GLOBAL")
                .effectiveFrom(now)
                .deleted(false)
                .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名、邮箱或手机号已存在");
        }
        return assembler.assemble(user);
    }

    private List<LoginIdentity> buildIdentities(RegisterCommand command, LocalDateTime now) {
        return Stream.of(
                buildIdentity(IdentityType.USERNAME, command.username(), true, now),
                buildIdentity(IdentityType.EMAIL, command.email(), false, now),
                buildIdentity(IdentityType.PHONE, command.phone(), false, now)
            )
            .flatMap(java.util.Optional::stream)
            .toList();
    }

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
            .deleted(false)
            .build());
    }

    private void ensureIdentityAvailable(LoginIdentity identity) {
        if (store.findIdentity(identity.type(), identity.normalizedValue()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名、邮箱或手机号已存在");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8
            || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码至少 8 位且必须包含字母和数字");
        }
    }
}
