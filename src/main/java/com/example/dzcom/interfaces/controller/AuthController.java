package com.example.dzcom.interfaces.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.dzcom.application.annotation.IgnoreLogin;
import com.example.dzcom.domain.constant.RedisKeyConstant;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.infrastructure.utils.CookieUtil;
import com.example.dzcom.infrastructure.utils.JwtUtil;
import com.example.dzcom.application.common.context.UserContext;
import com.example.dzcom.domain.model.User;
import com.example.dzcom.interfaces.vo.request.LoginRequest;
import com.example.dzcom.interfaces.vo.response.UserInfoVO;
import com.example.dzcom.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 认证管理控制器（Interfaces层）
 * <p>
 * 提供用户认证相关的 RESTful API 接口，包括登录、登出、获取当前用户信息等功能
 * 使用 Cookie + JWT + Redis 的认证方案，支持主动登出和 Token 自动过期
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "提供用户登录、登出、获取当前用户信息等认证相关功能")
@RequiredArgsConstructor
public class AuthController {
    
    /**
     * 用户服务
     */
    private final UserService userService;
    
    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;
    
    /**
     * Redis模板，用于管理Token
     */
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 用户登录
     * <p>
     * POST /api/auth/login
     * </p>
     * <p>
     * 登录流程：
     * 1. 验证用户名和密码是否正确
     * 2. 生成JWT Token
     * 3. 将Token存入Redis（7天过期）
     * 4. 将Token写入HttpOnly Cookie（防XSS攻击）
     * </p>
     *
     * @param request 登录请求（包含用户名和密码）
     * @param response HTTP响应对象，用于设置Cookie
     * @return 操作结果
     */
    @PostMapping("/login")
    @IgnoreLogin
    @Operation(
        summary = "用户登录",
        description = "验证用户名密码，登录成功后生成Token并写入Cookie和Redis，Token有效期7天"
    )
    public Result<Void> login(@Validated @RequestBody LoginRequest request, 
                              HttpServletResponse response) {
        
        // 1. 验证用户名密码
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        // 2. 生成JWT Token
        String token = jwtUtil.generateToken(user.getBizId(), user.getUsername());
        
        // 3. 存入Redis (7天过期)
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + user.getBizId();
        redisTemplate.opsForValue().set(redisKey, token, 7, TimeUnit.DAYS);
        
        // 4. 写入HttpOnly Cookie
        CookieUtil.setCookie(response, "AUTH_TOKEN", token, 7 * 24 * 3600);
        
        return Result.success(null);
    }
    
    /**
     * 用户登出
     * <p>
     * POST /api/auth/logout
     * </p>
     * <p>
     * 登出流程：
     * 1. 从当前上下文获取用户ID
     * 2. 删除Redis中的Token（实现主动登出）
     * 3. 删除Cookie中的Token
     * </p>
     *
     * @param response HTTP响应对象，用于删除Cookie
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(
        summary = "用户登出",
        description = "清除Redis和Cookie中的Token，实现主动登出功能"
    )
    public Result<Void> logout(HttpServletResponse response) {
        String userId = UserContext.getCurrentUserId();
        
        // 1. 删除Redis中的Token
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + userId;
        redisTemplate.delete(redisKey);
        
        // 2. 删除Cookie
        CookieUtil.deleteCookie(response, "AUTH_TOKEN");
        
        return Result.success(null);
    }
    
    /**
     * 获取当前登录用户信息
     * <p>
     * GET /api/auth/current
     * </p>
     *
     * @return 当前登录用户的详细信息
     */
    @GetMapping("/current")
    @Operation(
        summary = "获取当前用户信息",
        description = "从Token中解析用户ID，查询并返回当前登录用户的详细信息"
    )
    public Result<UserInfoVO> getCurrentUser() {
        String userId = UserContext.getCurrentUserId();
        User user = userService.getById(userId);
        
        UserInfoVO vo = BeanUtil.copyProperties(user, UserInfoVO.class);
        vo.setUserId(user.getBizId());
        return Result.success(vo);
    }
}
