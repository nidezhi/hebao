package com.example.dzcom.interfaces.dto.response.product;

import com.example.dzcom.application.dto.product.ProductView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 接口层产品响应，不直接暴露应用层或领域层对象。 */
@Builder
@Schema(description = "产品响应，包含交易参数、生命周期和扩展属性")
public record ProductResponse(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx") String bizId,
    @Schema(description = "产品编号", example = "P202600001") String productNo,
    @Schema(description = "产品编码", example = "AAPL") String productCode,
    @Schema(description = "产品名称", example = "Apple Inc.") String productName,
    @Schema(description = "产品类型", example = "STOCK") String productType,
    @Schema(description = "市场编码", example = "NASDAQ") String marketCode,
    @Schema(description = "币种", example = "USD") String currency,
    @Schema(description = "交易状态", example = "TRADABLE") String tradeStatus,
    @Schema(description = "风险等级", example = "3") int riskLevel,
    @Schema(description = "最小投资金额") BigDecimal minInvestAmount,
    @Schema(description = "金额步长") BigDecimal amountStep,
    @Schema(description = "数量步长") BigDecimal quantityStep,
    @Schema(description = "手续费率") BigDecimal feeRate,
    @Schema(description = "上市日期") LocalDate listingDate,
    @Schema(description = "退市日期") LocalDate delistingDate,
    @Schema(description = "产品说明") String description,
    @Schema(description = "扩展属性列表") List<ProductAttributeResponse> attributes,
    @Schema(description = "创建时间") LocalDateTime createdAt,
    @Schema(description = "最后更新时间") LocalDateTime updatedAt
) {

    /**
     * 将应用层产品视图转换为接口响应。
     *
     * @param source 应用层产品视图
     * @return 接口层产品响应
     * @author dz
     * @date 2026-06-15
     */
    public static ProductResponse from(ProductView source) {
        return ProductResponse.builder()
            .bizId(source.bizId())
            .productNo(source.productNo())
            .productCode(source.productCode())
            .productName(source.productName())
            .productType(source.productType() == null ? null : source.productType().name())
            .marketCode(source.marketCode())
            .currency(source.currency())
            .tradeStatus(source.tradeStatus() == null ? null : source.tradeStatus().name())
            .riskLevel(source.riskLevel())
            .minInvestAmount(source.minInvestAmount())
            .amountStep(source.amountStep())
            .quantityStep(source.quantityStep())
            .feeRate(source.feeRate())
            .listingDate(source.listingDate())
            .delistingDate(source.delistingDate())
            .description(source.description())
            .attributes(source.attributes() == null
                ? List.of()
                : source.attributes().stream().map(ProductAttributeResponse::from).toList())
            .createdAt(source.createdAt())
            .updatedAt(source.updatedAt())
            .build();
    }
}
