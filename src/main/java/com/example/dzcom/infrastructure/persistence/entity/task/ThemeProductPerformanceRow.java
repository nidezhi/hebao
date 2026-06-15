package com.example.dzcom.infrastructure.persistence.entity.task;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 行情收益统计查询行。 */
@Getter
@Setter
public class ThemeProductPerformanceRow {
    private String productBizId;
    private String productCode;
    private String productName;
    private BigDecimal startPrice;
    private BigDecimal endPrice;
    private BigDecimal returnRate;
}
