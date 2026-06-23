package com.example.dzcom.infrastructure.persistence.entity.portfolio;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 模拟组合持仓持久化实体。 */
@Schema(description = "模拟组合持仓持久化实体")
@TableName("aiw_position")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PositionEntity {
    /** 持仓业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "持仓业务唯一标识")
    private String bizId;
    /** 组合业务唯一标识。 */
    @Schema(description = "组合业务唯一标识")
    private String portfolioBizId;
    /** 产品业务唯一标识。 */
    @Schema(description = "产品业务唯一标识")
    private String productBizId;
    /** 持仓方向：LONG/SHORT。 */
    @Schema(description = "持仓方向：LONG/SHORT")
    private String positionSide;
    /** 当前持仓数量。 */
    @Schema(description = "当前持仓数量")
    private BigDecimal quantity;
    /** 当前可交易数量。 */
    @Schema(description = "当前可交易数量")
    private BigDecimal availableQuantity;
    /** 平均持仓成本。 */
    @Schema(description = "平均持仓成本")
    private BigDecimal averageCost;
    /** 持仓成本金额。 */
    @Schema(description = "持仓成本金额")
    private BigDecimal costAmount;
    /** 累计已实现盈亏。 */
    @Schema(description = "累计已实现盈亏")
    private BigDecimal realizedProfit;
    /** 最近一次影响该持仓的成交时间（北京时间）。 */
    @Schema(description = "最近一次影响该持仓的成交时间（北京时间）")
    private LocalDateTime lastTradeAt;
    /** 乐观锁版本号。 */
    @Schema(description = "乐观锁版本号")
    private int version;
    /** 记录创建时间（北京时间）。 */
    @Schema(description = "记录创建时间（北京时间）")
    private LocalDateTime createdAt;
    /** 记录最后更新时间（北京时间）。 */
    @Schema(description = "记录最后更新时间（北京时间）")
    private LocalDateTime updatedAt;
    /** 逻辑删除标记：0未删除、1已删除。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记：0未删除、1已删除")
    private int deleted;
}
