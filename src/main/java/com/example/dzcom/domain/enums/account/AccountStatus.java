package com.example.dzcom.domain.enums.account;

public enum AccountStatus {
    DISABLED(0),
    ACTIVE(1),
    LOCKED(2);

    /**
     * 创建并初始化 AccountStatus 对象。
     *
     * @param code code 参数
     * @author dz
     * @date 2026-06-14
     */
    private final int code;

    AccountStatus(int code) {
        this.code = code;
    }

    /**
     * 获取当前枚举对应的持久化编码。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public int code() {
        return code;
    }

    /**
     * 根据持久化编码解析枚举值。
     *
     * @param code code 参数
     * @return 方法执行后的结果
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    public static AccountStatus fromCode(int code) {
        for (AccountStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("无效的账户状态");
    }
}
