package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.RegisterCommand;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.AuthenticationApplicationService;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.infrastructure.security.account.AccountAuthenticationInterceptor;
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

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<UserView> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthenticationApplicationService.LoginResult login = service.login(request.account(), request.password());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(login.token().value(),
            login.token().maxAgeSeconds()).toString());
        return Result.success(login.user());
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Void> logout(HttpServletResponse response) {
        service.logout();
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", 0).toString());
        return Result.success();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户")
    public Result<UserView> me() {
        return Result.success(service.currentUser());
    }

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
