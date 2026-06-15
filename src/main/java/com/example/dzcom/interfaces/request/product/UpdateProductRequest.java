package com.example.dzcom.interfaces.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "管理端更新产品可变资料和交易参数的请求")
public record UpdateProductRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String bizId,
    @Schema(description = "产品名称", example = "Apple Inc.")
    @NotBlank @Size(max = 160) String productName,
    @Schema(description = "风险等级，允许值 1-5", example = "3")
    @Min(1) @Max(5) int riskLevel,
    @Schema(description = "最小投资金额", example = "100.0000")
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 4) BigDecimal minInvestAmount,
    @Schema(description = "金额步长", example = "1.0000")
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 4) BigDecimal amountStep,
    @Schema(description = "数量步长", example = "0.00010000")
    @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 8) BigDecimal quantityStep,
    @Schema(description = "手续费率，允许值 0-1", example = "0.00100000")
    @NotNull @DecimalMin("0") @DecimalMax("1") @Digits(integer = 4, fraction = 8) BigDecimal feeRate,
    @Schema(description = "上市日期", example = "2026-01-01")
    LocalDate listingDate,
    @Schema(description = "退市日期", example = "2028-01-01")
    LocalDate delistingDate,
    @Schema(description = "产品说明，最多 5000 字符")
    @Size(max = 5000) String description
) {
}
