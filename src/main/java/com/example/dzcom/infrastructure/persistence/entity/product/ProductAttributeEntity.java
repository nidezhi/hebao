package com.example.dzcom.infrastructure.persistence.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 产品扩展属性实体，使用标量产品业务 ID 维持逻辑关联。 */
@Schema(description = "持久化：产品扩展属性表（aiw_product_attribute）")
@TableName("aiw_product_attribute")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductAttributeEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    @Schema(description = "关联产品业务标识")
    private String productBizId;
    @Schema(description = "属性键")
    private String attributeKey;
    @Schema(description = "值类型")
    private String valueType;
    @Schema(description = "属性值（JSON 文本）")
    private String attributeValue;
    @Schema(description = "生效日期")
    private LocalDate effectiveDate;
    @Schema(description = "来源编码")
    private String sourceCode;
    @Schema(description = "创建时间（北京时间）")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间（北京时间）")
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
}
