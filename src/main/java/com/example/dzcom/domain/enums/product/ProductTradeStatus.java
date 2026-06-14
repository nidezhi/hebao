package com.example.dzcom.domain.enums.product;

/**
 * 产品交易状态及其数据库稳定编码。
 */
public enum ProductTradeStatus {
    DISABLED(0),
    TRADABLE(1),
    SUSPENDED(2);

    /**
     * 创建并初始化 ProductTradeStatus 对象。
     *
     * @param code code 参数
     * @author dz
     * @date 2026-06-14
     */
    private final int code;

    ProductTradeStatus(int code) {
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
     * 将持久化编码转换为领域枚举，拒绝静默接收未知状态。
     *
     * @param code code 参数
     * @return 方法执行后的结果
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    public static ProductTradeStatus fromCode(int code) {
        for (ProductTradeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的产品交易状态: " + code);
    }
}
