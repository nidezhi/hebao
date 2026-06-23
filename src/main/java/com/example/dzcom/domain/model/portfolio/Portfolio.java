package com.example.dzcom.domain.model.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资组合领域对象，当前优先承载模拟组合。 */
@Builder(toBuilder = true)
@Schema(description = "投资组合领域对象，当前优先承载模拟组合")
public record Portfolio(
    @Schema(description = "组合业务唯一标识")
    String bizId,
    @Schema(description = "组合展示编号")
    String portfolioNo,
    @Schema(description = "组合所有者用户业务标识")
    String ownerUserBizId,
    @Schema(description = "组合名称")
    String portfolioName,
    @Schema(description = "组合类型：PERSONAL/MODEL/SIMULATION")
    String portfolioType,
    @Schema(description = "组合基础计价币种")
    String baseCurrency,
    @Schema(description = "组合状态：0关闭、1正常、2冻结")
    int status,
    @Schema(description = "乐观锁版本号")
    int version,
    @Schema(description = "记录创建时间（北京时间）")
    LocalDateTime createdAt,
    @Schema(description = "记录最后更新时间（北京时间）")
    LocalDateTime updatedAt,
    @Schema(description = "创建操作者业务标识")
    String createdBy,
    @Schema(description = "最后更新操作者业务标识")
    String updatedBy,
    @Schema(description = "逻辑删除标记：0未删除、1已删除")
    int deleted,
    @Schema(description = "逻辑删除时间（北京时间）")
    LocalDateTime deletedAt
) {
}
