package com.example.dzcom.domain.model.task;

import lombok.Builder;

import java.math.BigDecimal;

/** 单个产品在指定窗口内的收益表现。 */
@Builder
public record ThemeProductPerformance(
    String productBizId,
    String productCode,
    String productName,
    BigDecimal startPrice,
    BigDecimal endPrice,
    BigDecimal returnRate
) {
}
