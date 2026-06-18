package com.example.dzcom.application.command.product;

import com.example.dzcom.domain.enums.product.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 管理端创建产品目录项的用例输入。 */
@Builder
@Schema(description = "创建产品目录项的应用层命令")
public record CreateProductCommand(
    @Schema(description = "市场或渠道内产品编码")
    String productCode,
    @Schema(description = "产品展示名称")
    String productName,
    @Schema(description = "产品类型")
    ProductType productType,
    @Schema(description = "产品所属市场编码")
    String marketCode,
    @Schema(description = "产品计价币种")
    String currency,
    @Schema(description = "产品风险等级，范围 1-5")
    int riskLevel,
    @Schema(description = "最小投资金额")
    BigDecimal minInvestAmount,
    @Schema(description = "投资金额递增步长")
    BigDecimal amountStep,
    @Schema(description = "交易数量递增步长")
    BigDecimal quantityStep,
    @Schema(description = "产品交易费率")
    BigDecimal feeRate,
    @Schema(description = "上市日期")
    LocalDate listingDate,
    @Schema(description = "退市日期")
    LocalDate delistingDate,
    @Schema(description = "产品业务说明")
    String description
) {
}
