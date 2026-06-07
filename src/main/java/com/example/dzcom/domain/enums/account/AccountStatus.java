package com.example.dzcom.domain.enums.account;

public enum AccountStatus {
    DISABLED(0),
    ACTIVE(1),
    LOCKED(2);

    private final int code;

    AccountStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static AccountStatus fromCode(int code) {
        for (AccountStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("无效的账户状态");
    }
}
