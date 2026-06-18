package com.example.dzcom.application.command.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 更新产品可变资料的用例输入；产品代码、市场、类型和币种属于稳定身份，不允许修改。 */
@Builder
@Schema(description = "更新产品可变资料的应用层命令")
public record UpdateProductCommand(
    @Schema(description = "更新后的产品名称")
    String productName,
    @Schema(description = "更新后的风险等级，范围 1-5")
    int riskLevel,
    @Schema(description = "更新后的最小投资金额")
    BigDecimal minInvestAmount,
    @Schema(description = "更新后的投资金额步长")
    BigDecimal amountStep,
    @Schema(description = "更新后的交易数量步长")
    BigDecimal quantityStep,
    @Schema(description = "更新后的交易费率")
    BigDecimal feeRate,
    @Schema(description = "更新后的上市日期")
    LocalDate listingDate,
    @Schema(description = "更新后的退市日期")
    LocalDate delistingDate,
    @Schema(description = "更新后的产品说明")
    String description
) {
}
