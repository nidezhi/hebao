package com.example.dzcom.interfaces.request.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 产品稳定身份不可修改，该请求只包含允许变化的资料和交易参数。 */
public record UpdateProductRequest(
    @NotBlank String bizId,
    @NotBlank @Size(max = 160) String productName,
    @Min(1) @Max(5) int riskLevel,
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 4) BigDecimal minInvestAmount,
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 4) BigDecimal amountStep,
    @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 8) BigDecimal quantityStep,
    @NotNull @DecimalMin("0") @DecimalMax("1") @Digits(integer = 4, fraction = 8) BigDecimal feeRate,
    LocalDate listingDate,
    LocalDate delistingDate,
    @Size(max = 5000) String description
) {
}
