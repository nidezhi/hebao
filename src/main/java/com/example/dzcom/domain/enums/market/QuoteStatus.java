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

    private final int code;

    QuoteStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public boolean visible() {
        return this != INVALID;
    }

    public static QuoteStatus fromCode(int code) {
        for (QuoteStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的行情状态: " + code);
    }
}
