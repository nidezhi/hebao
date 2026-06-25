package com.example.dzcom.infrastructure.config;

import com.example.dzcom.infrastructure.config.account.AccountAuthenticationInterceptor;
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

    /**
     * 执行 add interceptors 处理。
     *
     * @param registry registry 参数
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accountAuthenticationInterceptor)
            .addPathPatterns(
                "/api/auth/me",
                "/api/users/**",
                "/api/admin/**",
                "/api/mock/portfolios/**",
                "/api/backtests/**",
                "/api/ai/feedback/**",
                "/api/ai/prompt-evaluations/**",
                "/api/ai/prompts/**"
            );
    }
}
