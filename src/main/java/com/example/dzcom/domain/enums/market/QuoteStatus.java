package com.example.dzcom.domain.enums.market;

/**
 * 行情数据质量状态。
 *
 * <p>{@link #CORRECTED} 表示数据源对原行情点进行了修正，它仍属于可查询的有效数据；
 * {@link #INVALID} 仅保留追溯价值，不应进入面向用户的最新行情和历史序列。</p>
 */
public enum QuoteStatus {
    INVALID(0),
    VALID(1),
    CORRECTED(2);

    /**
     * 创建并初始化 QuoteStatus 对象。
     *
     * @param code code 参数
     * @author dz
     * @date 2026-06-14
     */
    private final int code;

    QuoteStatus(int code) {
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
     * 执行 visible 处理。
     *
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public boolean visible() {
        return this != INVALID;
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
    public static QuoteStatus fromCode(int code) {
        for (QuoteStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的行情状态: " + code);
    }
}
