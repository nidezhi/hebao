package com.example.dzcom.infrastructure.persistence.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 产品主题、行业、指数和资产类别关系持久化实体。 */
@Schema(description = "产品主题行业指数关系持久化实体")
@TableName("aiw_product_theme_relation")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductThemeRelationEntity {
    /** 关系业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "关系业务唯一标识")
    private String bizId;
    /** 产品业务唯一标识。 */
    @Schema(description = "产品业务唯一标识")
    private String productBizId;
    /** 关系类型。 */
    @Schema(description = "关系类型：THEME/INDUSTRY/INDEX/ASSET_CLASS")
    private String relationType;
    /** 关系稳定编码。 */
    @Schema(description = "关系稳定编码")
    private String relationCode;
    /** 关系展示名称。 */
    @Schema(description = "关系展示名称")
    private String relationName;
    /** 关系权重。 */
    @Schema(description = "关系权重，0-1")
    private BigDecimal relationWeight;
    /** 关系来源编码。 */
    @Schema(description = "关系来源编码")
    private String sourceCode;
    /** 关系证据摘要。 */
    @Schema(description = "关系证据摘要")
    private String evidence;
    /** 创建时间，北京时间。 */
    @Schema(description = "创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 更新时间，北京时间。 */
    @Schema(description = "更新时间，北京时间")
    private LocalDateTime updatedAt;
}
