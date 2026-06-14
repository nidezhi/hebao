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

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 产品扩展属性实体，使用标量产品业务 ID 维持逻辑关联。 */
@TableName("aiw_product_attribute")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductAttributeEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String productBizId;
    private String attributeKey;
    private String valueType;
    private String attributeValue;
    private LocalDate effectiveDate;
    private String sourceCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    private int deleted;
}
