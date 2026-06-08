package com.example.dzcom.infrastructure.persistence.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
 * {@code productBizId} 在各自仓储中查询，不建立 JPA 对象图和级联关系。</p>
 */
@Entity
@Table(name = "aiw_product")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "product_no", nullable = false, length = 32)
    private String productNo;
    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;
    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;
    @Column(name = "product_type", nullable = false, length = 32)
    private String productType;
    @Column(name = "market_code", nullable = false, length = 32)
    private String marketCode;
    @Column(nullable = false, length = 8)
    private String currency;
    @Column(name = "trade_status", nullable = false, columnDefinition = "TINYINT")
    private int tradeStatus;
    @Column(name = "risk_level", nullable = false, columnDefinition = "TINYINT")
    private int riskLevel;
    @Column(name = "min_invest_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal minInvestAmount;
    @Column(name = "amount_step", nullable = false, precision = 20, scale = 4)
    private BigDecimal amountStep;
    @Column(name = "quantity_step", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantityStep;
    @Column(name = "fee_rate", nullable = false, precision = 12, scale = 8)
    private BigDecimal feeRate;
    @Column(name = "listing_date")
    private LocalDate listingDate;
    @Column(name = "delisting_date")
    private LocalDate delistingDate;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Version
    @Column(nullable = false)
    private int version;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "created_by", length = 64)
    private String createdBy;
    @Column(name = "updated_by", length = 64)
    private String updatedBy;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT")
    private int deleted;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
