package com.example.dzcom.infrastructure.security.account;

import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.domain.model.account.User;
import com.example.dzcom.domain.model.account.UserCredential;
import com.example.dzcom.domain.repository.account.AccountStore;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 账户 Cookie 会话认证拦截器。
 *
 * <p>每次受保护请求都会验证 Redis 会话、账户状态和凭据版本，
 * 验证成功后将当前操作者写入线程上下文，并在请求结束时清理。</p>
 */
@Component
@RequiredArgsConstructor
public class AccountAuthenticationInterceptor implements HandlerInterceptor {
    public static final String COOKIE_NAME = "DZCOM_SESSION";

    private final SessionService sessions;
    private final AccountStore store;
    private final CurrentOperatorContext context;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = readCookie(request);
        SessionService.SessionData session = sessions.resolve(token)
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期"));
        User user = store.findUser(session.userBizId())
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期"));
        if (!user.canLogin()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        UserCredential credential = store.findPasswordCredential(user.getBizId())
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期"));
        if (credential.credentialVersion() != session.credentialVersion()) {
            sessions.revoke(token);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        context.set(new CurrentOperator(user.getBizId(), token, session.roles()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        context.clear();
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        return Arrays.stream(cookies)
            .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse("");
    }
}
