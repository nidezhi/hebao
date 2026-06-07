package com.example.dzcom.domain.enums.account;

public enum KycStatus {
    UNVERIFIED(0),
    VERIFIED(1),
    REVIEWING(2),
    REJECTED(3);

    private final int code;

    KycStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static KycStatus fromCode(int code) {
        for (KycStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("无效的 KYC 状态");
    }
}
