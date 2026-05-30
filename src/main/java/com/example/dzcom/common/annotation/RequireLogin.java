package com.example.dzcom.common.annotation;

import java.lang.annotation.*;

/**
 * 需要登录注解
 * 标记需要用户登录才能访问的接口（默认所有接口都需要登录）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
