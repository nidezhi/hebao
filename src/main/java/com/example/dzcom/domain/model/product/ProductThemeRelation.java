package com.example.dzcom.domain.model.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 产品与主题、行业、指数或资产类别之间的显式关系领域对象。 */
@Builder
@Schema(description = "产品主题行业指数关系领域对象")
public record ProductThemeRelation(
    @Schema(description = "关系业务唯一标识")
    String bizId,
    @Schema(description = "产品业务唯一标识")
    String productBizId,
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
    String evidence,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "记录更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
