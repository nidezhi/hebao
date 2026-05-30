package com.example.dzcom.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.dzcom.common.annotation.IgnoreLogin;
import com.example.dzcom.common.constant.RedisKeyConstant;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.utils.CookieUtil;
import com.example.dzcom.common.utils.JwtUtil;
import com.example.dzcom.context.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) throws Exception {
        
        // 检查是否有忽略登录的注解
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(IgnoreLogin.class)) {
                return true;
            }
        }
        
        // 从Cookie获取Token
        String token = CookieUtil.getCookieValue(request, "AUTH_TOKEN");
        
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        
        // 验证JWT
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "Token无效");
        }
        
        // 获取用户ID
        String userId = jwtUtil.getUserIdFromToken(token);
        
        // 检查Redis中Token是否存在
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + userId;
        String redisToken = redisTemplate.opsForValue().get(redisKey);
        
        if (StrUtil.isBlank(redisToken) || !redisToken.equals(token)) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        
        // 解析用户信息
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.get("username", String.class);
        
        // 设置到ThreadLocal
        UserContext.setCurrentUser(
            UserContext.UserInfo.builder()
                .userId(userId)
                .username(username)
                .build()
        );
        
        log.debug("用户登录验证通过: userId={}, username={}", userId, username);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler, 
                                Exception ex) throws Exception {
        // 清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
