package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.command.account.UpdateIdentitiesCommand;
import com.example.dzcom.application.service.account.AccountRegistrationService;
import com.example.dzcom.application.service.account.AuthorizationService;
import com.example.dzcom.application.service.account.PermissionCodes;
import com.example.dzcom.application.service.account.UserApplicationService;
import com.example.dzcom.application.service.account.UserQueryService;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.interfaces.dto.response.account.UserResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.account.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端用户接口。
 *
 * <p>所有请求必须先通过会话认证，具体用例再次校验细粒度权限，
 * 状态、KYC 和风险等级使用独立接口，避免通用更新绕过业务规则。</p>
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "管理端用户", description = "管理用户查询、创建、资料、状态、KYC、风险等级和逻辑删除的接口")
public class AdminUserController {
    private final UserQueryService queries;
    private final UserApplicationService users;
    private final AccountRegistrationService registration;
    private final AuthorizationService authorization;

    /**
     * 根据筛选条件分页查询用户列表。
     *
     * @param request 用户筛选、分页和排序请求
     * @return 用户分页结果
     * @throws BusinessException 当分页参数或排序规则不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Operation(summary = "用户列表", description = "按关键字、状态、KYC、风险等级分页查询用户。分页参数默认 page=1,size=20, sort=createdAt,direction=desc。\n\n请求中 page 可为 0 用于兼容前端零基页码，会被转换为 1。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回接口层用户分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "请求参数校验失败或分页参数不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户列表查看权限"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    public Result<PageResponse<UserResponse>> list(@Valid @RequestBody AdminUserListRequest request) {
        var result = queries.list(
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
        );
        return Result.success(PageResponse.from(result, UserResponse::from));
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
    @Operation(summary = "用户详情", description = "根据用户业务标识查询完整的接口层用户响应。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户详情查看权限"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> detail(@Valid @RequestBody UserBizIdRequest request) {
        return Result.success(UserResponse.from(queries.detail(request.bizId())));
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
    @Operation(summary = "管理端创建用户", description = "由管理员创建用户。请求遵循公开注册的用户名/密码规则。可选设置初始账户状态（默认 ACTIVE）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功，返回新建用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或用户名/密码不符合规则"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "操作者无权限创建用户"),
        @ApiResponse(responseCode = "409", description = "用户标识冲突"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> create(@Valid @RequestBody AdminCreateUserRequest request) {
        authorization.require(PermissionCodes.ACCOUNT_USER_CREATE);
        var created = registration.register(RegisterCommand.builder()
            .username(request.username())
            .password(request.password())
            .email(request.email())
            .phone(request.phone())
            .nickname(request.nickname())
            .initialRole("USER")
            .build());
        if (request.status() != null && !"ACTIVE".equals(request.status().name())) {
            return Result.success(UserResponse.from(
                users.changeStatus(created.bizId(), request.status())));
        }
        return Result.success(UserResponse.from(created));
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
    @Operation(summary = "更新用户邮箱和手机号", description = "根据用户 bizId 更新用户的 email 与 phone。变更后会做唯一性冲突校验。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回更新后的用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户更新权限"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "409", description = "登录标识冲突"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> update(@Valid @RequestBody AdminUpdateUserRequest request) {
        return Result.success(UserResponse.from(users.updateUser(
            request.bizId(), UpdateIdentitiesCommand.builder()
            .email(request.email())
            .phone(request.phone())
            .build())));
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
    @Operation(summary = "更新账户状态", description = "变更用户账户状态（ACTIVE/DISABLED/LOCKED）。请使用专用接口避免绕过状态机规则。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回状态变更后的用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或状态转换不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户状态变更权限"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> status(@Valid @RequestBody StatusRequest request) {
        return Result.success(UserResponse.from(
            users.changeStatus(request.bizId(), request.status())));
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
    @Operation(summary = "更新 KYC 状态", description = "变更用户的 KYC 状态（UNVERIFIED/VERIFIED/REVIEWING/REJECTED）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回更新后的用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或状态转换不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无 KYC 状态变更权限"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> kycStatus(@Valid @RequestBody KycStatusRequest request) {
        return Result.success(UserResponse.from(
            users.changeKycStatus(request.bizId(), request.kycStatus())));
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
    @Operation(summary = "更新风险等级", description = "设置用户风险承受等级，允许值 1-5。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回更新后的用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数非法（riskLevel 必须 1-5）"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无风险等级变更权限"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> riskLevel(@Valid @RequestBody RiskLevelRequest request) {
        return Result.success(UserResponse.from(
            users.changeRiskLevel(request.bizId(), request.riskLevel())));
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
    @Operation(summary = "软删除用户", description = "对用户执行逻辑删除，并撤销相关会话。此操作不可恢复，应由具有足够权限的管理员执行。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功（Result<Void>）", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户删除权限"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<Void> delete(@Valid @RequestBody UserBizIdRequest request) {
        users.deleteUser(request.bizId());
        return Result.success();
    }

}
