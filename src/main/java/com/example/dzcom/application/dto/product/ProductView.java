package com.example.dzcom.application.dto.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品目录对外视图。
 *
 * <p>列表查询可返回空属性集合，详情查询再装配扩展属性，避免分页读取产生 N+1 查询。</p>
 */
@Builder
public record ProductView(
    String bizId,
    String productNo,
    String productCode,
    String productName,
    ProductType productType,
    String marketCode,
    String currency,
    ProductTradeStatus tradeStatus,
    int riskLevel,
    BigDecimal minInvestAmount,
    BigDecimal amountStep,
    BigDecimal quantityStep,
    BigDecimal feeRate,
    LocalDate listingDate,
    LocalDate delistingDate,
    String description,
    List<ProductAttributeView> attributes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
