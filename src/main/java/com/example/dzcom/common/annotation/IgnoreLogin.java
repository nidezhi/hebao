package com.example.dzcom.common.annotation;

import java.lang.annotation.*;

/**
 * 忽略登录注解
 * 标记不需要登录即可访问的接口（如登录、注册等公开接口）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreLogin {
}
