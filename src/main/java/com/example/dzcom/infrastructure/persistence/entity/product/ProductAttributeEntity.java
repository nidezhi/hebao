package com.example.dzcom.infrastructure.persistence.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 产品扩展属性实体，使用标量产品业务 ID 维持逻辑关联。 */
@Entity
@Table(name = "aiw_product_attribute")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductAttributeEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "product_biz_id", nullable = false, length = 36)
    private String productBizId;
    @Column(name = "attribute_key", nullable = false, length = 64)
    private String attributeKey;
    @Column(name = "value_type", nullable = false, length = 16)
    private String valueType;
    @Column(name = "attribute_value", nullable = false, columnDefinition = "json")
    private String attributeValue;
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    @Column(name = "source_code", length = 64)
    private String sourceCode;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT")
    private int deleted;
}
