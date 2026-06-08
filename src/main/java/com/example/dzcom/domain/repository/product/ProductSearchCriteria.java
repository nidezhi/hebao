package com.example.dzcom.domain.repository.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;

/** 与具体分页框架解耦的产品目录查询条件。 */
public record ProductSearchCriteria(
    String keyword,
    ProductType productType,
    ProductTradeStatus tradeStatus,
    Integer riskLevel,
    String currency,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
