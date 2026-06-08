package com.example.dzcom.application.command.product;

import com.example.dzcom.domain.enums.product.ProductType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 管理端创建产品目录项的用例输入。 */
@Builder
public record CreateProductCommand(
    String productCode,
    String productName,
    ProductType productType,
    String marketCode,
    String currency,
    int riskLevel,
    BigDecimal minInvestAmount,
    BigDecimal amountStep,
    BigDecimal quantityStep,
    BigDecimal feeRate,
    LocalDate listingDate,
    LocalDate delistingDate,
    String description
) {
}
