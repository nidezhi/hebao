package com.example.dzcom.infrastructure.config;

import com.example.dzcom.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final LoginInterceptor loginInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/api/**")           // 拦截所有API
            .excludePathPatterns(
                "/api/auth/login",                // 登录接口排除
                "/api/auth/register",             // 注册接口排除
                "/doc.html",                      // Knife4j文档
                "/swagger-ui/**",
                "/v3/api-docs/**"
            );
    }
}
