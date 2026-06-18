package com.example.dzcom.application.dto.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "产品目录应用层视图")
public record ProductView(
    @Schema(description = "产品业务唯一标识")
    String bizId,
    @Schema(description = "平台内部产品编号")
    String productNo,
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
    @Schema(description = "产品交易状态")
    ProductTradeStatus tradeStatus,
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
    @Schema(description = "产品上市日期")
    LocalDate listingDate,
    @Schema(description = "产品退市日期")
    LocalDate delistingDate,
    @Schema(description = "产品业务说明")
    String description,
    @Schema(description = "产品扩展属性集合")
    List<ProductAttributeView> attributes,
    @Schema(description = "产品创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "产品最后更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
