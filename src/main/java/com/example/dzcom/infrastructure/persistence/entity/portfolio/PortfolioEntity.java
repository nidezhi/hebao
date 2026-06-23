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

import java.time.LocalDateTime;

/** 投资组合持久化实体。 */
@Schema(description = "投资组合持久化实体")
@TableName("aiw_portfolio")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioEntity {
    /** 组合业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "组合业务唯一标识")
    private String bizId;
    /** 组合展示编号。 */
    @Schema(description = "组合展示编号")
    private String portfolioNo;
    /** 组合所有者用户业务标识。 */
    @Schema(description = "组合所有者用户业务标识")
    private String ownerUserBizId;
    /** 组合名称。 */
    @Schema(description = "组合名称")
    private String portfolioName;
    /** 组合类型：PERSONAL/MODEL/SIMULATION。 */
    @Schema(description = "组合类型：PERSONAL/MODEL/SIMULATION")
    private String portfolioType;
    /** 基础计价币种。 */
    @Schema(description = "基础计价币种")
    private String baseCurrency;
    /** 组合状态：0关闭、1正常、2冻结。 */
    @Schema(description = "组合状态：0关闭、1正常、2冻结")
    private int status;
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
    /** 最后更新操作者业务标识。 */
    @Schema(description = "最后更新操作者业务标识")
    private String updatedBy;
    /** 逻辑删除标记：0未删除、1已删除。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记：0未删除、1已删除")
    private int deleted;
    /** 逻辑删除时间（北京时间）。 */
    @Schema(description = "逻辑删除时间（北京时间）")
    private LocalDateTime deletedAt;
}
