package com.example.dzcom.infrastructure.config;

import com.example.dzcom.infrastructure.config.account.AccountAuthenticationInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 Web MVC 认证拦截路径，避免新增依赖当前操作者的接口遗漏会话解析。
 */
class WebMvcConfigTest {

    /**
     * 校验 Mock 组合等当前用户接口会进入账户认证拦截器。
     *
     * @author dz
     * @date 2026-06-24
     */
    @Test
    void currentOperatorEndpointsMustBeProtectedByAuthenticationInterceptor() {
        AccountAuthenticationInterceptor interceptor =
            new AccountAuthenticationInterceptor(null, null, null, null, null);
        ExposedInterceptorRegistry registry = new ExposedInterceptorRegistry();
        new WebMvcConfig(interceptor).addInterceptors(registry);

        List<MappedInterceptor> mappedInterceptors = registry.exposedInterceptors().stream()
            .filter(MappedInterceptor.class::isInstance)
            .map(MappedInterceptor.class::cast)
            .filter(mappedInterceptor -> mappedInterceptor.getInterceptor() == interceptor)
            .toList();

        assertProtected(mappedInterceptors, "/api/mock/portfolios/mine");
        assertProtected(mappedInterceptors, "/api/mock/portfolios/orders/buy");
        assertProtected(mappedInterceptors, "/api/backtests/detail");
        assertProtected(mappedInterceptors, "/api/ai/feedback/save");
        assertProtected(mappedInterceptors, "/api/ai/prompt-evaluations/list");
        assertProtected(mappedInterceptors, "/api/ai/prompts/save");
    }

    /**
     * 断言指定路径会命中账户认证拦截器。
     *
     * @param mappedInterceptors 已注册的映射拦截器集合
     * @param path 需要校验的接口路径
     * @author dz
     * @date 2026-06-24
     */
    private void assertProtected(List<MappedInterceptor> mappedInterceptors, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        ServletRequestPathUtils.parseAndCache(request);
        assertTrue(mappedInterceptors.stream().anyMatch(mappedInterceptor -> mappedInterceptor.matches(request)),
            path + " 必须进入账户认证拦截器，确保 CurrentOperatorProvider 可读取当前用户");
    }

    /**
     * 暴露 Spring MVC 注册结果，供测试读取最终 MappedInterceptor。
     */
    private static class ExposedInterceptorRegistry extends InterceptorRegistry {

        /**
         * 返回 WebMvcConfig 注册后的拦截器对象集合。
         *
         * @return 拦截器对象集合
         * @author dz
         * @date 2026-06-24
         */
        List<Object> exposedInterceptors() {
            return getInterceptors();
        }
    }
}
