package com.example.dzcom.infrastructure.config;

import com.example.dzcom.infrastructure.security.account.AccountAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 基础配置。
 *
 * Web MVC 基础配置。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final AccountAuthenticationInterceptor accountAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accountAuthenticationInterceptor)
            .addPathPatterns("/api/auth/me", "/api/users/**", "/api/admin/**");
    }
}
