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

/** 模拟订单持久化实体。 */
@Schema(description = "模拟订单持久化实体")
@TableName("aiw_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockOrderEntity {
    /** 订单业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "订单业务唯一标识")
    private String bizId;
    /** 订单展示编号。 */
    @Schema(description = "订单展示编号")
    private String orderNo;
    /** 客户端幂等键。 */
    @Schema(description = "客户端幂等键")
    private String idempotencyKey;
    /** 下单用户业务标识。 */
    @Schema(description = "下单用户业务标识")
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
    /** 订单方向。 */
    @Schema(description = "订单方向")
    private String orderSide;
    /** 订单类型。 */
    @Schema(description = "订单类型")
    private String orderType;
    /** 订单币种。 */
    @Schema(description = "订单币种")
    private String currency;
    /** 委托价格。 */
    @Schema(description = "委托价格")
    private BigDecimal requestedPrice;
    /** 委托数量。 */
    @Schema(description = "委托数量")
    private BigDecimal requestedQuantity;
    /** 委托金额。 */
    @Schema(description = "委托金额")
    private BigDecimal requestedAmount;
    /** 累计成交数量。 */
    @Schema(description = "累计成交数量")
    private BigDecimal executedQuantity;
    /** 累计成交金额。 */
    @Schema(description = "累计成交金额")
    private BigDecimal executedAmount;
    /** 累计费用。 */
    @Schema(description = "累计费用")
    private BigDecimal feeAmount;
    /** 订单状态。 */
    @Schema(description = "订单状态")
    private String status;
    /** 外部订单编号。 */
    @Schema(description = "外部订单编号")
    private String externalOrderId;
    /** 拒绝原因编码。 */
    @Schema(description = "拒绝原因编码")
    private String rejectCode;
    /** 拒绝原因说明。 */
    @Schema(description = "拒绝原因说明")
    private String rejectMessage;
    /** 提交时间（北京时间）。 */
    @Schema(description = "提交时间（北京时间）")
    private LocalDateTime submittedAt;
    /** 完成时间（北京时间）。 */
    @Schema(description = "完成时间（北京时间）")
    private LocalDateTime completedAt;
    /** 乐观锁版本号。 */
    @Schema(description = "乐观锁版本号")
    private int version;
    /** 记录创建时间（北京时间）。 */
    @Schema(description = "记录创建时间（北京时间）")
    private LocalDateTime createdAt;
    /** 记录最后更新时间（北京时间）。 */
    @Schema(description = "记录最后更新时间（北京时间）")
    private LocalDateTime updatedAt;
    /** 创建操作者业务标识。 */
    @Schema(description = "创建操作者业务标识")
    private String createdBy;
    /** 逻辑删除标记：0未删除、1已删除。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记：0未删除、1已删除")
    private int deleted;
}
