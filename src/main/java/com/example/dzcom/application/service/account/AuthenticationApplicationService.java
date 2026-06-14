package com.example.dzcom.application.service.account;

import com.example.dzcom.application.assembler.account.AccountViewAssembler;
import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.model.account.LoginIdentity;
import com.example.dzcom.domain.model.account.User;
import com.example.dzcom.domain.model.account.UserCredential;
import com.example.dzcom.domain.repository.account.LoginIdentityStore;
import com.example.dzcom.domain.repository.account.UserCredentialStore;
import com.example.dzcom.domain.repository.account.UserRoleStore;
import com.example.dzcom.domain.repository.account.UserStore;
import com.example.dzcom.domain.service.account.IdentityNormalizer;
import com.example.dzcom.domain.service.account.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationApplicationService {
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final AccountRegistrationService registrationService;
    private final UserStore users;
    private final LoginIdentityStore identities;
    private final UserCredentialStore credentials;
    private final AuthorizationService authorization;
    private final IdentityNormalizer normalizer;
    private final PasswordHasher passwordHasher;
    private final SessionService sessions;
    private final CurrentOperatorProvider currentOperator;
    private final ClockProvider clock;
    private final AccountViewAssembler assembler;

    /**
     * 创建或保存对应的业务数据。
     *
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public UserView register(RegisterCommand command) {
        return registrationService.register(command);
    }

    /**
     * 执行 login 处理。
     *
     * @param account account 参数
     * @param password password 参数
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public LoginResult login(String account, String password) {
        IdentityType type = normalizer.detectType(account);
        String normalized = normalizer.normalize(type, account);
        LoginIdentity identity = identities.findByTypeAndNormalizedValue(type, normalized)
            .orElseThrow(this::invalidCredentials);
        User user = users.findByBizId(identity.userBizId()).orElseThrow(this::invalidCredentials);
        UserCredential credential = credentials.findPasswordByUserBizId(user.getBizId())
            .orElseThrow(this::invalidCredentials);
        LocalDateTime now = clock.now();
        if (!user.canLogin()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账户不可登录");
        }
        if (credential.isLocked(now)) {
            throw invalidCredentials();
        }
        if (!passwordHasher.matches(password, credential.secretHash())) {
            LocalDateTime lockedUntil = credential.failedAttempts() + 1 >= MAX_FAILED_ATTEMPTS
                ? now.plusMinutes(15) : null;
            credentials.save(credential.failed(lockedUntil));
            throw invalidCredentials();
        }
        credentials.save(credential.loginSucceeded());
        user.recordSuccessfulLogin(now);
        users.save(user);
        AuthorizationService.AuthorizationSnapshot snapshot = authorization.resolve(user.getBizId());
        SessionService.SessionToken token = sessions.create(
            user.getBizId(),
            credential.credentialVersion(),
            snapshot.roleCodes(),
            snapshot.permissionCodes()
        );
        return new LoginResult(assembler.assemble(user), token);
    }

    /**
     * 执行 logout 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    public void logout() {
        sessions.revoke(currentOperator.required().sessionToken());
    }

    /**
     * 执行 current user 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public UserView currentUser() {
        User user = users.findByBizId(currentOperator.required().userBizId())
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期"));
        return assembler.assemble(user);
    }

    /**
     * 执行 invalid credentials 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private BusinessException invalidCredentials() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
    }

    public record LoginResult(UserView user, SessionService.SessionToken token) {
    }
}
