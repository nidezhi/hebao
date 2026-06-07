package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.command.account.UpdateIdentitiesCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.AccountRegistrationService;
import com.example.dzcom.application.service.account.UserApplicationService;
import com.example.dzcom.application.service.account.UserQueryService;
import com.example.dzcom.common.page.PageQuery;
import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.interfaces.request.account.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端用户接口。
 *
 * <p>所有请求必须先通过会话认证，具体用例再次校验 ADMIN 角色，
 * 状态、KYC 和风险等级使用独立接口，避免通用更新绕过业务规则。</p>
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "管理端用户")
public class AdminUserController {
    private final UserQueryService queries;
    private final UserApplicationService users;
    private final AccountRegistrationService registration;
    private final CurrentOperatorProvider currentOperator;

    @GetMapping
    @Operation(summary = "用户列表")
    public Result<PageResult<UserView>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) AccountStatus status,
        @RequestParam(required = false) KycStatus kycStatus,
        @RequestParam(required = false) Integer riskLevel,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return Result.success(queries.list(keyword, status, kycStatus, riskLevel,
            new PageQuery(page, size, sort, direction)));
    }

    @GetMapping("/{bizId}")
    @Operation(summary = "用户详情")
    public Result<UserView> detail(@PathVariable String bizId) {
        return Result.success(queries.detail(bizId));
    }

    @PostMapping
    @Operation(summary = "管理端创建用户")
    public Result<UserView> create(@Valid @RequestBody AdminCreateUserRequest request) {
        requireAdmin();
        UserView created = registration.register(RegisterCommand.builder()
            .username(request.username())
            .password(request.password())
            .email(request.email())
            .phone(request.phone())
            .nickname(request.nickname())
            .initialRole("USER")
            .build());
        if (request.status() != null && request.status() != AccountStatus.ACTIVE) {
            return Result.success(users.changeStatus(created.bizId(), request.status()));
        }
        return Result.success(created);
    }

    @PatchMapping("/{bizId}")
    @Operation(summary = "更新用户邮箱和手机号")
    public Result<UserView> update(@PathVariable String bizId, @Valid @RequestBody UpdateUserRequest request) {
        return Result.success(users.updateUser(bizId, UpdateIdentitiesCommand.builder()
            .email(request.email())
            .phone(request.phone())
            .build()));
    }

    @PatchMapping("/{bizId}/status")
    @Operation(summary = "更新账户状态")
    public Result<UserView> status(@PathVariable String bizId, @Valid @RequestBody StatusRequest request) {
        return Result.success(users.changeStatus(bizId, request.status()));
    }

    @PatchMapping("/{bizId}/kyc-status")
    @Operation(summary = "更新 KYC 状态")
    public Result<UserView> kycStatus(@PathVariable String bizId,
                                      @Valid @RequestBody KycStatusRequest request) {
        return Result.success(users.changeKycStatus(bizId, request.kycStatus()));
    }

    @PatchMapping("/{bizId}/risk-level")
    @Operation(summary = "更新风险等级")
    public Result<UserView> riskLevel(@PathVariable String bizId,
                                      @Valid @RequestBody RiskLevelRequest request) {
        return Result.success(users.changeRiskLevel(bizId, request.riskLevel()));
    }

    @DeleteMapping("/{bizId}")
    @Operation(summary = "软删除用户")
    public Result<Void> delete(@PathVariable String bizId) {
        users.deleteUser(bizId);
        return Result.success();
    }

    private void requireAdmin() {
        if (!currentOperator.required().hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }
}
