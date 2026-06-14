package com.example.dzcom.application.assembler.product;

import com.example.dzcom.application.dto.product.ProductAttributeView;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductAttribute;
import org.springframework.stereotype.Component;

import java.util.List;

/** 将产品领域数据转换为稳定的接口视图。 */
@Component
public class ProductViewAssembler {
    /**
     * 组装列表项；刻意不读取扩展属性，避免分页查询产生 N+1。
     *
     * @param product product 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    public ProductView assembleSummary(Product product) {
        return assemble(product, List.of());
    }

    /**
     * 组装包含低频扩展属性的产品详情。
     *
     * @param product product 参数
     * @param attributes attributes 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    public ProductView assembleDetail(Product product, List<ProductAttribute> attributes) {
        return assemble(product, attributes.stream().map(this::toView).toList());
    }

    /**
     * 将源对象转换为目标视图或领域对象。
     *
     * @param product product 参数
     * @param attributes attributes 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    private ProductView assemble(Product product, List<ProductAttributeView> attributes) {
        return ProductView.builder()
            .bizId(product.getBizId())
            .productNo(product.getProductNo())
            .productCode(product.getProductCode())
            .productName(product.getProductName())
            .productType(product.getProductType())
            .marketCode(product.getMarketCode())
            .currency(product.getCurrency())
            .tradeStatus(product.getTradeStatus())
            .riskLevel(product.getRiskLevel())
            .minInvestAmount(product.getMinInvestAmount())
            .amountStep(product.getAmountStep())
            .quantityStep(product.getQuantityStep())
            .feeRate(product.getFeeRate())
            .listingDate(product.getListingDate())
            .delistingDate(product.getDelistingDate())
            .description(product.getDescription())
            .attributes(attributes)
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }

    /**
     * 将源对象转换为目标视图或领域对象。
     *
     * @param attribute attribute 参数
     * @return 转换后的目标对象
     * @author dz
     * @date 2026-06-14
     */
    private ProductAttributeView toView(ProductAttribute attribute) {
        return ProductAttributeView.builder()
            .key(attribute.key())
            .valueType(attribute.valueType())
            .jsonValue(attribute.jsonValue())
            .effectiveDate(attribute.effectiveDate())
            .sourceCode(attribute.sourceCode())
            .build();
    }
}
