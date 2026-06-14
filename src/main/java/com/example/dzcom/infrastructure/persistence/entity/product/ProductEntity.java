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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品主数据持久化实体。
 *
 * <p>实体只映射 {@code aiw_product} 的标量字段。行情、属性、持仓和订单均通过
 * {@code productBizId} 在各自仓储中查询，不建立持久化对象图和级联关系。</p>
 */
@TableName("aiw_product")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String productNo;
    private String productCode;
    private String productName;
    private String productType;
    private String marketCode;
    private String currency;
    private int tradeStatus;
    private int riskLevel;
    private BigDecimal minInvestAmount;
    private BigDecimal amountStep;
    private BigDecimal quantityStep;
    private BigDecimal feeRate;
    private LocalDate listingDate;
    private LocalDate delistingDate;
    private String description;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    @TableField("is_deleted")
    private int deleted;
    private LocalDateTime deletedAt;
}
