package com.example.dzcom.application.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/** 模拟投资组合应用层视图。 */
@Builder
@Schema(description = "模拟投资组合应用层视图")
public record MockPortfolioView(
    @Schema(description = "组合业务唯一标识")
    String bizId,
    @Schema(description = "组合展示编号")
    String portfolioNo,
    @Schema(description = "组合所有者用户业务标识")
    String ownerUserBizId,
    @Schema(description = "组合名称")
    String portfolioName,
    @Schema(description = "组合类型，当前固定为 SIMULATION")
    String portfolioType,
    @Schema(description = "基础计价币种")
    String baseCurrency,
    @Schema(description = "组合状态：0关闭、1正常、2冻结")
    int status,
    @Schema(description = "最新估值快照；列表或无估值时可为空")
    PortfolioValuationView latestValuation,
    @Schema(description = "当前持仓集合；列表页为空，详情页返回")
    List<PositionView> positions,
    @Schema(description = "记录创建时间（北京时间）")
    LocalDateTime createdAt,
    @Schema(description = "记录最后更新时间（北京时间）")
    LocalDateTime updatedAt
) {
}
