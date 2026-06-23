package com.example.dzcom.infrastructure.persistence.entity.portfolio;

import com.baomidou.mybatisplus.annotation.IdType;
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

/** 模拟成交持久化实体。 */
@Schema(description = "模拟成交持久化实体")
@TableName("aiw_trade_execution")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeExecutionEntity {
    /** 成交业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "成交业务唯一标识")
    private String bizId;
    /** 成交展示编号。 */
    @Schema(description = "成交展示编号")
    private String executionNo;
    /** 订单业务唯一标识。 */
    @Schema(description = "订单业务唯一标识")
    private String orderBizId;
    /** 用户业务唯一标识。 */
    @Schema(description = "用户业务唯一标识")
    private String userBizId;
    /** 组合业务唯一标识。 */
    @Schema(description = "组合业务唯一标识")
    private String portfolioBizId;
    /** 产品业务唯一标识。 */
    @Schema(description = "产品业务唯一标识")
    private String productBizId;
    /** 执行渠道编码。 */
    @Schema(description = "执行渠道编码")
    private String channelCode;
    /** 外部成交编号。 */
    @Schema(description = "外部成交编号")
    private String externalExecutionId;
    /** 成交价格。 */
    @Schema(description = "成交价格")
    private BigDecimal executionPrice;
    /** 成交数量。 */
    @Schema(description = "成交数量")
    private BigDecimal executionQuantity;
    /** 成交金额。 */
    @Schema(description = "成交金额")
    private BigDecimal executionAmount;
    /** 本笔成交费用。 */
    @Schema(description = "本笔成交费用")
    private BigDecimal feeAmount;
    /** 成交时间（北京时间）。 */
    @Schema(description = "成交时间（北京时间）")
    private LocalDateTime executedAt;
    /** 记录创建时间（北京时间）。 */
    @Schema(description = "记录创建时间（北京时间）")
    private LocalDateTime createdAt;
}
