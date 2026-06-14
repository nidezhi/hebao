package com.example.dzcom.application.service.account;

import com.example.dzcom.application.assembler.account.AccountViewAssembler;
import com.example.dzcom.application.command.account.UpdateIdentitiesCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.service.ClockProvider;
import com.example.dzcom.common.service.IdGenerator;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.domain.model.account.*;
import com.example.dzcom.domain.repository.account.AccountStore;
import com.example.dzcom.application.service.account.*;
import com.example.dzcom.domain.service.account.IdentityNormalizer;
import com.example.dzcom.domain.service.account.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final AccountStore store;
    private final CurrentOperatorProvider currentOperator;
    private final IdentityNormalizer normalizer;
    private final PasswordHasher passwordHasher;
    private final SessionService sessions;
    private final ClockProvider clock;
    private final IdGenerator idGenerator;
    private final AccountViewAssembler assembler;

    /**
     * 更新对应的业务数据。
     *
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public UserView updateCurrentUser(UpdateIdentitiesCommand command) {
        String userBizId = currentOperator.required().userBizId();
        updateIdentity(userBizId, IdentityType.EMAIL, command.email());
        updateIdentity(userBizId, IdentityType.PHONE, command.phone());
        return assembler.assemble(requiredUser(userBizId));
    }

    /**
     * 更新对应的业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public UserView updateUser(String bizId, UpdateIdentitiesCommand command) {
        requireAdmin();
        requiredUser(bizId);
        updateIdentity(bizId, IdentityType.EMAIL, command.email());
        updateIdentity(bizId, IdentityType.PHONE, command.phone());
        return assembler.assemble(requiredUser(bizId));
    }

    /**
     * 执行 change password 处理。
     *
     * @param currentPassword currentPassword 参数
     * @param newPassword newPassword 参数
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        validatePassword(newPassword);
        String userBizId = currentOperator.required().userBizId();
        UserCredential credential = store.findPasswordCredential(userBizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "密码凭据不存在"));
        if (!passwordHasher.matches(currentPassword, credential.secretHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前密码错误");
        }
        store.saveCredential(credential.changeSecret(passwordHasher.hash(newPassword), clock.now()));
        sessions.revokeAll(userBizId);
    }

    /**
     * 执行 change status 处理。
     *
     * @param bizId 业务对象的唯一标识
     * @param status 目标状态或目标值
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public UserView changeStatus(String bizId, AccountStatus status) {
        requireAdmin();
        User user = requiredUser(bizId);
        user.changeStatus(status, clock.now());
        store.saveUser(user);
        if (status != AccountStatus.ACTIVE) {
            sessions.revokeAll(bizId);
        }
        return assembler.assemble(user);
    }

    /**
     * 执行 change kyc status 处理。
     *
     * @param bizId 业务对象的唯一标识
     * @param status 目标状态或目标值
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public UserView changeKycStatus(String bizId, KycStatus status) {
        requireAdmin();
        requiredUser(bizId);
        UserRiskProfile risk = requiredRiskProfile(bizId);
        store.saveRiskProfile(risk.changeKycStatus(status, clock.now()));
        return assembler.assemble(requiredUser(bizId));
    }

    /**
     * 执行 change risk level 处理。
     *
     * @param bizId 业务对象的唯一标识
     * @param riskLevel riskLevel 参数
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public UserView changeRiskLevel(String bizId, int riskLevel) {
        requireAdmin();
        if (riskLevel < 1 || riskLevel > 5) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "风险等级必须在 1 到 5 之间");
        }
        requiredUser(bizId);
        UserRiskProfile risk = requiredRiskProfile(bizId);
        store.saveRiskProfile(risk.changeRiskLevel(riskLevel, clock.now()));
        return assembler.assemble(requiredUser(bizId));
    }

    /**
     * 删除或逻辑删除对应的业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public void deleteUser(String bizId) {
        requireAdmin();
        store.findUser(bizId).ifPresent(user -> {
            user.delete(clock.now());
            store.softDeleteAccountData(bizId);
            store.saveUser(user);
            sessions.revokeAll(bizId);
        });
    }

    /**
     * 更新对应的业务数据。
     *
     * @param userBizId 业务对象的唯一标识
     * @param type 数据类型
     * @param value 待处理的数据值
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void updateIdentity(String userBizId, IdentityType type, String value) {
        if (value == null) {
            return;
        }
        String normalized = normalizer.normalize(type, value);
        store.findIdentity(type, normalized).ifPresent(existing -> {
            if (!existing.userBizId().equals(userBizId)) {
                throw new BusinessException(HttpStatus.CONFLICT, "邮箱或手机号已被使用");
            }
        });
        LoginIdentity identity = store.findIdentity(userBizId, type)
            .map(existing -> existing.toBuilder()
                .value(value.trim())
                .normalizedValue(normalized)
                .verified(existing.verified() && existing.normalizedValue().equals(normalized))
                .active(true)
                .deleted(0)
                .build())
            .orElseGet(() -> LoginIdentity.builder()
                .bizId(idGenerator.newBizId())
                .userBizId(userBizId)
                .type(type)
                .value(value.trim())
                .normalizedValue(normalized)
                .verified(false)
                .active(true)
                .createdAt(clock.now())
                .deleted(0)
                .build());
        store.saveIdentity(identity);
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    private User requiredUser(String bizId) {
        return store.findUser(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    private UserRiskProfile requiredRiskProfile(String bizId) {
        return store.findRiskProfile(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户风险画像不存在"));
    }

    /**
     * 执行 require admin 处理。
     *
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void requireAdmin() {
        if (!currentOperator.required().hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
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
            throw new BusinessException(HttpStatus.BAD_REQUEST, "新密码至少 8 位且必须包含字母和数字");
        }
    }
}
