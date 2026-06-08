package com.example.dzcom.application.command.product;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 更新产品可变资料的用例输入；产品代码、市场、类型和币种属于稳定身份，不允许修改。 */
@Builder
public record UpdateProductCommand(
    String productName,
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
