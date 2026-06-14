package com.example.dzcom.interfaces.request.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 产品目录分页查询请求，筛选条件和分页参数统一从请求体接收。
 */
public record ProductListRequest(
    String keyword,
    ProductType productType,
    ProductTradeStatus tradeStatus,
    @Min(1) @Max(5) Integer riskLevel,
    String currency,
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    String sort,
    String direction
) {
}
