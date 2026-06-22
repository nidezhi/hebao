package com.example.dzcom.application.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 产品主题、行业、指数或资产类别关系应用层视图。 */
@Builder
@Schema(description = "产品主题行业指数关系应用层视图")
public record ProductThemeRelationView(
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
}
