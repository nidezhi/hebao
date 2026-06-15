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
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 管理端创建产品请求。 */
@Schema(description = "管理端创建产品请求，包含基础交易参数")
public record CreateProductRequest(
    @Schema(description = "产品编码，唯一", example = "AAPL") @NotBlank @Size(max = 64) String productCode,
    @Schema(description = "产品名称", example = "Apple Inc.") @NotBlank @Size(max = 160) String productName,
    @Schema(description = "产品类型") @NotNull ProductType productType,
    @Schema(description = "市场编码", example = "NASDAQ") @NotBlank @Size(max = 32) String marketCode,
    @Schema(description = "币种", example = "USD") @NotBlank @Size(min = 3, max = 8) String currency,
    @Schema(description = "风险等级（1-5）") @Min(1) @Max(5) int riskLevel,
    @Schema(description = "最小投资金额") @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 4) BigDecimal minInvestAmount,
    @Schema(description = "金额步长") @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 4) BigDecimal amountStep,
    @Schema(description = "数量步长") @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 8) BigDecimal quantityStep,
    @Schema(description = "费率（0-1）") @NotNull @DecimalMin("0") @DecimalMax("1") @Digits(integer = 4, fraction = 8) BigDecimal feeRate,
    @Schema(description = "上市日期（UTC）", example = "2026-01-01") LocalDate listingDate,
    @Schema(description = "退市日期（UTC）", example = "2028-01-01") LocalDate delistingDate,
    @Schema(description = "产品说明") @Size(max = 5000) String description
) {
}
