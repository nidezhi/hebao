package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.account.AuthenticationApplicationService;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.infrastructure.config.account.AccountAuthenticationInterceptor;
import com.example.dzcom.interfaces.dto.response.account.UserResponse;
import com.example.dzcom.interfaces.request.account.LoginRequest;
import com.example.dzcom.interfaces.request.account.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 账户认证接口，提供注册、登录、登出和当前用户查询。
 *
 * <p>Controller 只完成请求转换和 Cookie 协议处理，认证规则和事务均位于 Service。</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "账户认证", description = "用户注册、登录、登出和当前会话用户查询接口")
public class AuthenticationController {
    private final AuthenticationApplicationService service;

    /**
     * 注册普通用户。
     *
     * @param request 用户注册请求
     * @return HTTP 201 状态和注册后的用户信息
     * @throws BusinessException 当登录标识冲突或注册规则不满足时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/register")
    @Operation(summary = "注册用户", description = "公开注册普通用户。用户名、密码、email 与 phone 的格式在请求边界校验。密码须包含字母和数字，长度 8-72。")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "注册成功，返回创建后的用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或注册规则不满足"),
        @ApiResponse(responseCode = "409", description = "登录标识（username/email/phone）冲突"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public ResponseEntity<Result<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var user = service.register(RegisterCommand.builder()
            .username(request.username())
            .password(request.password())
            .email(request.email())
            .phone(request.phone())
            .nickname(request.nickname())
            .initialRole("USER")
            .build());
        return ResponseEntity.status(201).body(Result.success(UserResponse.from(user)));
    }

    /**
     * 校验账号密码并创建登录会话。
     *
     * @param request 登录凭据
     * @param response 用于写入会话 Cookie 的 HTTP 响应
     * @return 当前登录用户信息
     * @throws BusinessException 当账号密码错误或账户不可登录时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/login")
    @Operation(summary = "登录", description = "使用用户名/邮箱/手机号 + 密码登录。成功后在响应头写入会话 Cookie (DZCOM_SESSION)。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功，返回当前用户响应，并在 Set-Cookie 中返回会话令牌", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "账号或密码错误或账户不可登录"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthenticationApplicationService.LoginResult login = service.login(request.account(), request.password());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(login.token().value(),
            login.token().maxAgeSeconds()).toString());
        return Result.success(UserResponse.from(login.user()));
    }

    /**
     * 撤销当前会话并清除会话 Cookie。
     *
     * @param response 用于清除会话 Cookie 的 HTTP 响应
     * @return 无业务数据的成功结果
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/logout")
    @Operation(summary = "登出", description = "撤销当前会话并清除会话 Cookie（设置空 token 并 max-age=0）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登出成功（Result<Void>）", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "401", description = "未登录或会话已过期"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<Void> logout(HttpServletResponse response) {
        service.logout();
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", 0).toString());
        return Result.success();
    }

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前登录用户信息
     * @throws BusinessException 当用户未登录或会话失效时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/me")
    @Operation(summary = "获取当前用户", description = "返回当前登录用户信息。受会话认证拦截器保护，未登录将返回 401。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回当前用户响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> me() {
        return Result.success(UserResponse.from(service.currentUser()));
    }

    /**
     * 构建登录会话 Cookie。
     *
     * @param value 会话令牌值
     * @param maxAge Cookie 最大存活秒数
     * @return 安全属性统一配置后的会话 Cookie
     * @author dz
     * @date 2026-06-14
     */
    private ResponseCookie sessionCookie(String value, long maxAge) {
        return ResponseCookie.from(AccountAuthenticationInterceptor.COOKIE_NAME, value)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(maxAge)
            .build();
    }
}
