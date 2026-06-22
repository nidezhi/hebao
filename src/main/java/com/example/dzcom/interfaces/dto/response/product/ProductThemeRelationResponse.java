package com.example.dzcom.interfaces.dto.response.product;

import com.example.dzcom.application.dto.product.ProductThemeRelationView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 产品主题、行业、指数和资产类别关系响应。 */
@Builder
@Schema(description = "产品主题行业指数关系响应")
public record ProductThemeRelationResponse(
    @Schema(description = "关系类型：THEME/INDUSTRY/INDEX/ASSET_CLASS")
    String relationType,
    @Schema(description = "关系稳定编码")
    String relationCode,
    @Schema(description = "关系展示名称")
    String relationName,
    @Schema(description = "关系权重，0-1")
    BigDecimal relationWeight,
    @Schema(description = "关系来源编码")
    String sourceCode,
    @Schema(description = "关系证据摘要")
    String evidence
) {
    /**
     * 将应用视图转换为接口响应。
     *
     * @param view 产品主题关系应用视图
     * @return 产品主题关系响应
     * @author dz
     * @date 2026-06-22
     */
    public static ProductThemeRelationResponse from(ProductThemeRelationView view) {
        return ProductThemeRelationResponse.builder()
            .relationType(view.relationType())
            .relationCode(view.relationCode())
            .relationName(view.relationName())
            .relationWeight(view.relationWeight())
            .sourceCode(view.sourceCode())
            .evidence(view.evidence())
            .build();
    }
}
