package com.example.dzcom.interfaces.request.product;

import com.example.dzcom.domain.enums.product.ProductType;
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

/** 管理端创建产品请求。 */
public record CreateProductRequest(
    @NotBlank @Size(max = 64) String productCode,
    @NotBlank @Size(max = 160) String productName,
    @NotNull ProductType productType,
    @NotBlank @Size(max = 32) String marketCode,
    @NotBlank @Size(min = 3, max = 8) String currency,
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
