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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品主数据持久化实体。
 *
 * <p>实体只映射 {@code aiw_product} 的标量字段。行情、属性、持仓和订单均通过
 * {@code productBizId} 在各自仓储中查询，不建立持久化对象图和级联关系。</p>
 */
@Schema(description = "持久化：产品主表实体（aiw_product）")
@TableName("aiw_product")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    @Schema(description = "产品编号")
    private String productNo;
    @Schema(description = "产品编码")
    private String productCode;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "产品类型")
    private String productType;
    @Schema(description = "市场编码")
    private String marketCode;
    @Schema(description = "币种")
    private String currency;
    @Schema(description = "交易状态编码")
    private int tradeStatus;
    @Schema(description = "风险等级")
    private int riskLevel;
    @Schema(description = "最小投资金额")
    private BigDecimal minInvestAmount;
    @Schema(description = "金额步长")
    private BigDecimal amountStep;
    @Schema(description = "数量步长")
    private BigDecimal quantityStep;
    @Schema(description = "费率")
    private BigDecimal feeRate;
    @Schema(description = "上市日期")
    private LocalDate listingDate;
    @Schema(description = "退市日期")
    private LocalDate delistingDate;
    @Schema(description = "产品说明")
    private String description;
    @Schema(description = "乐观锁版本")
    private int version;
    @Schema(description = "记录创建时间（北京时间）")
    private LocalDateTime createdAt;
    @Schema(description = "记录最后更新时间（北京时间）")
    private LocalDateTime updatedAt;
    @Schema(description = "创建者标识")
    private String createdBy;
    @Schema(description = "最后修改者标识")
    private String updatedBy;
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
    @Schema(description = "删除时间（北京时间）")
    private LocalDateTime deletedAt;
}
