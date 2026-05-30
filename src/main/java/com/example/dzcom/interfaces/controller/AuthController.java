package com.example.dzcom.interfaces.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.dzcom.common.annotation.IgnoreLogin;
import com.example.dzcom.common.constant.RedisKeyConstant;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.common.utils.CookieUtil;
import com.example.dzcom.common.utils.JwtUtil;
import com.example.dzcom.context.UserContext;
import com.example.dzcom.infrastructure.dao.entity.User;
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
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、注册、登出")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    
    @PostMapping("/login")
    @IgnoreLogin
    @Operation(summary = "用户登录", description = "登录成功后Token写入Cookie")
    public Result<Void> login(@Validated @RequestBody LoginRequest request, 
                              HttpServletResponse response) {
        
        // 验证用户名密码
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getBizId(), user.getUsername());
        
        // 存入Redis (7天过期)
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + user.getBizId();
        redisTemplate.opsForValue().set(redisKey, token, 7, TimeUnit.DAYS);
        
        // 写入Cookie
        CookieUtil.setCookie(response, "AUTH_TOKEN", token, 7 * 24 * 3600);
        
        return Result.success(null);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除Token")
    public Result<Void> logout(HttpServletResponse response) {
        String userId = UserContext.getCurrentUserId();
        
        // 删除Redis中的Token
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + userId;
        redisTemplate.delete(redisKey);
        
        // 删除Cookie
        CookieUtil.deleteCookie(response, "AUTH_TOKEN");
        
        return Result.success(null);
    }
    
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> getCurrentUser() {
        String userId = UserContext.getCurrentUserId();
        User user = userService.getById(userId);
        
        UserInfoVO vo = BeanUtil.copyProperties(user, UserInfoVO.class);
        vo.setUserId(user.getBizId());
        return Result.success(vo);
    }
}
