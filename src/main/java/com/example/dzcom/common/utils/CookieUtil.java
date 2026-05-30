package com.example.dzcom.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Cookie工具类
 */
public class CookieUtil {
    
    /**
     * 设置Cookie
     */
    public static void setCookie(HttpServletResponse response, 
                                  String name, 
                                  String value, 
                                  int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)              // 防止XSS
            .secure(false)               // 生产环境改为true (HTTPS)
            .path("/")
            .maxAge(Duration.ofSeconds(maxAge))
            .sameSite("Lax")             // CSRF防护
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    /**
     * 删除Cookie
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    /**
     * 从Request获取Cookie值
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    private CookieUtil() {
        // 防止实例化
    }
}
