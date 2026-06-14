package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.command.account.UpdateIdentitiesCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.AccountRegistrationService;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.account.UserApplicationService;
import com.example.dzcom.application.service.account.UserQueryService;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.domain.enums.account.AccountStatus;
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

    /**
     * 根据筛选条件分页查询用户列表。
     *
     * @param request 用户筛选、分页和排序请求
     * @return 用户分页结果
     * @throws BusinessException 当分页参数或排序规则不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Operation(summary = "用户列表")
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    public Result<PageResult<UserView>> list(@Valid @RequestBody AdminUserListRequest request) {
        return Result.success(queries.list(
            request.keyword(),
            request.status(),
            request.kycStatus(),
            request.riskLevel(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "createdAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ));
    }

    /**
     * 根据用户业务标识查询用户详情。
     *
     * @param request 用户业务标识请求
     * @return 用户详细信息
     * @throws BusinessException 当用户不存在时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/detail")
    @Operation(summary = "用户详情")
    public Result<UserView> detail(@Valid @RequestBody UserBizIdRequest request) {
        return Result.success(queries.detail(request.bizId()));
    }

    /**
     * 由管理员创建用户并设置可选的初始账户状态。
     *
     * @param request 管理端创建用户请求
     * @return 创建后的用户信息
     * @throws BusinessException 当操作者无权限或用户标识冲突时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/create")
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

    /**
     * 更新指定用户的邮箱和手机号登录标识。
     *
     * @param request 用户业务标识和待更新资料
     * @return 更新后的用户信息
     * @throws BusinessException 当用户不存在或登录标识冲突时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/update")
    @Operation(summary = "更新用户邮箱和手机号")
    public Result<UserView> update(@Valid @RequestBody AdminUpdateUserRequest request) {
        return Result.success(users.updateUser(request.bizId(), UpdateIdentitiesCommand.builder()
            .email(request.email())
            .phone(request.phone())
            .build()));
    }

    /**
     * 变更指定用户的账户状态。
     *
     * @param request 用户业务标识和目标账户状态
     * @return 状态变更后的用户信息
     * @throws BusinessException 当用户不存在或状态转换不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/status")
    @Operation(summary = "更新账户状态")
    public Result<UserView> status(@Valid @RequestBody StatusRequest request) {
        return Result.success(users.changeStatus(request.bizId(), request.status()));
    }

    /**
     * 变更指定用户的 KYC 状态。
     *
     * @param request 用户业务标识和目标 KYC 状态
     * @return KYC 状态变更后的用户信息
     * @throws BusinessException 当用户不存在或状态转换不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/kyc-status")
    @Operation(summary = "更新 KYC 状态")
    public Result<UserView> kycStatus(@Valid @RequestBody KycStatusRequest request) {
        return Result.success(users.changeKycStatus(request.bizId(), request.kycStatus()));
    }

    /**
     * 变更指定用户的风险承受等级。
     *
     * @param request 用户业务标识和目标风险等级
     * @return 风险等级变更后的用户信息
     * @throws BusinessException 当用户不存在或风险等级不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/risk-level")
    @Operation(summary = "更新风险等级")
    public Result<UserView> riskLevel(@Valid @RequestBody RiskLevelRequest request) {
        return Result.success(users.changeRiskLevel(request.bizId(), request.riskLevel()));
    }

    /**
     * 逻辑删除指定用户并撤销相关会话。
     *
     * @param request 用户业务标识请求
     * @return 无业务数据的成功结果
     * @throws BusinessException 当用户不存在或不允许删除时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/delete")
    @Operation(summary = "软删除用户")
    public Result<Void> delete(@Valid @RequestBody UserBizIdRequest request) {
        users.deleteUser(request.bizId());
        return Result.success();
    }

    /**
     * 校验当前操作者是否具备管理员角色。
     *
     * @throws BusinessException 当当前操作者不具备管理员角色时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void requireAdmin() {
        if (!currentOperator.required().hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }
}
