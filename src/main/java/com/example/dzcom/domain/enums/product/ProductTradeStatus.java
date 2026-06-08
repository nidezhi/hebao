package com.example.dzcom.domain.enums.product;

/**
 * 产品交易状态及其数据库稳定编码。
 */
public enum ProductTradeStatus {
    DISABLED(0),
    TRADABLE(1),
    SUSPENDED(2);

    private final int code;

    ProductTradeStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    /** 将持久化编码转换为领域枚举，拒绝静默接收未知状态。 */
    public static ProductTradeStatus fromCode(int code) {
        for (ProductTradeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的产品交易状态: " + code);
    }
}
