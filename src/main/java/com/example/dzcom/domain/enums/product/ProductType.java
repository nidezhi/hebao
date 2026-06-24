package com.example.dzcom.domain.enums.product;

/**
 * 平台当前支持的标准产品类型。
 *
 * <p>枚举只收纳已经进入产品统一模型的稳定类型。新增类型前需要确认其交易单位、
 * 价格精度和生命周期是否能复用现有产品规则，避免把不同性质的资产强行归类。</p>
 */
public enum ProductType {
    STOCK,
    FUND,
    BOND,
    ETF,
    BANK_WMP
}
