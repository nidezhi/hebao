package com.example.dzcom.application.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "success"),
    
    /**
     * 失败
     */
    ERROR(500, "系统错误"),
    
    /**
     * 参数校验失败
     */
    VALIDATION_ERROR(400, "参数校验失败"),
    
    /**
     * 未登录或登录已过期
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    
    /**
     * 权限不足
     */
    FORBIDDEN(403, "权限不足"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    CONFLICT(409, "资源冲突"),
    
    /**
     * 业务异常
     */
    BUSINESS_ERROR(400, "业务异常");
    
    private final Integer code;
    private final String message;
}
