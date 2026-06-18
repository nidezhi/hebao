package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资主题收益、动量和资讯热度快照持久化实体。
 *
 * <p>每条记录表示一个任务在指定统计窗口内对单个投资主题生成的可解释指标。</p>
 */
@Schema(description = "投资主题收益、动量和资讯热度快照持久化实体")
@TableName("aiw_investment_theme_snapshot")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentThemeSnapshotEntity {
    /** 快照业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "快照业务唯一标识")
    private String bizId;
    /** 生成该快照的任务编码。 */
    @Schema(description = "来源任务编码")
    private String taskCode;
    /** 快照指标类型。 */
    @Schema(description = "快照类型：RETURN/MOMENTUM/NEWS_HEAT")
    private String snapshotType;
    /** 投资主题稳定编码。 */
    @Schema(description = "投资主题稳定编码")
    private String themeCode;
    /** 投资主题展示名称。 */
    @Schema(description = "投资主题展示名称")
    private String themeName;
    /** 快照所属市场范围。 */
    @Schema(description = "市场范围", example = "CN_MAINLAND")
    private String marketScope;
    /** 指标计算回看窗口分钟数。 */
    @Schema(description = "统计窗口分钟数")
    private int windowMinutes;
    /** 实际参与计算的行情或资讯样本数。 */
    @Schema(description = "参与计算的样本数量")
    private int sampleCount;
    /** 窗口平均收益率，小数形式。 */
    @Schema(description = "窗口平均收益率，小数形式")
    private BigDecimal returnRate;
    /** 平均收益与上涨广度组合得到的动量分数。 */
    @Schema(description = "市场动量分数")
    private BigDecimal momentumScore;
    /** 资讯关键词命中数量形成的热度分数。 */
    @Schema(description = "资讯热度分数")
    private BigDecimal heatScore;
    /** 窗口内收益表现最好的产品业务标识。 */
    @Schema(description = "窗口内表现最佳产品业务标识")
    private String topProductBizId;
    /** 计算样本和解释指标 JSON。 */
    @Schema(description = "可解释指标和样本明细 JSON 字符串")
    private String metrics;
    /** 指标对应的业务快照时间，北京时间。 */
    @Schema(description = "业务快照时间，北京时间")
    private LocalDateTime snapshotTime;
    /** 数据创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
}
