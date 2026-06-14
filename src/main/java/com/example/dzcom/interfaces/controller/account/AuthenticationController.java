package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.AuthenticationApplicationService;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.infrastructure.config.account.AccountAuthenticationInterceptor;
import com.example.dzcom.interfaces.request.account.LoginRequest;
import com.example.dzcom.interfaces.request.account.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "账户认证")
public class AuthenticationController {
    private final AuthenticationApplicationService service;

    /**
     * 注册普通用户。
     *
     * @param request 用户注册请求
     * @return HTTP 201 状态和注册后的用户信息
     * @throws com.example.dzcom.common.exception.BusinessException 当登录标识冲突或注册规则不满足时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/register")
    @Operation(summary = "注册用户")
    public ResponseEntity<Result<UserView>> register(@Valid @RequestBody RegisterRequest request) {
        UserView user = service.register(RegisterCommand.builder()
            .username(request.username())
            .password(request.password())
            .email(request.email())
            .phone(request.phone())
            .nickname(request.nickname())
            .initialRole("USER")
            .build());
        return ResponseEntity.status(201).body(Result.success(user));
    }

    /**
     * 校验账号密码并创建登录会话。
     *
     * @param request 登录凭据
     * @param response 用于写入会话 Cookie 的 HTTP 响应
     * @return 当前登录用户信息
     * @throws com.example.dzcom.common.exception.BusinessException 当账号密码错误或账户不可登录时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<UserView> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthenticationApplicationService.LoginResult login = service.login(request.account(), request.password());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(login.token().value(),
            login.token().maxAgeSeconds()).toString());
        return Result.success(login.user());
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
    @Operation(summary = "登出")
    public Result<Void> logout(HttpServletResponse response) {
        service.logout();
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", 0).toString());
        return Result.success();
    }

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前登录用户信息
     * @throws com.example.dzcom.common.exception.BusinessException 当用户未登录或会话失效时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/me")
    @Operation(summary = "获取当前用户")
    public Result<UserView> me() {
        return Result.success(service.currentUser());
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
