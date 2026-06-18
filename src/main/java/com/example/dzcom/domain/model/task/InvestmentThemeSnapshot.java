package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 单个投资主题在指定窗口内的收益、动量或资讯热度快照。 */
@Builder
@Schema(description = "投资主题分析快照领域对象")
public record InvestmentThemeSnapshot(
    @Schema(description = "快照业务唯一标识")
    String bizId,
    @Schema(description = "来源任务编码")
    String taskCode,
    @Schema(description = "快照类型：RETURN/MOMENTUM/NEWS_HEAT")
    String snapshotType,
    @Schema(description = "投资主题稳定编码")
    String themeCode,
    @Schema(description = "投资主题展示名称")
    String themeName,
    @Schema(description = "快照所属市场范围")
    String marketScope,
    @Schema(description = "统计回看窗口分钟数")
    int windowMinutes,
    @Schema(description = "参与统计的样本数量")
    int sampleCount,
    @Schema(description = "窗口平均收益率")
    BigDecimal returnRate,
    @Schema(description = "市场动量分数")
    BigDecimal momentumScore,
    @Schema(description = "资讯热度分数")
    BigDecimal heatScore,
    @Schema(description = "窗口内表现最佳产品业务标识")
    String topProductBizId,
    @Schema(description = "计算样本和解释指标 JSON")
    String metrics,
    @Schema(description = "业务快照时间，北京时间")
    LocalDateTime snapshotTime,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt
) {
}
