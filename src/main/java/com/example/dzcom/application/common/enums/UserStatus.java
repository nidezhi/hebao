package com.example.dzcom.application.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserStatus {
    
    /**
     * 正常
     */
    ACTIVE(1, "正常"),
    
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    
    /**
     * 锁定
     */
    LOCKED(-1, "锁定");
    
    private final Integer code;
    private final String description;
}
