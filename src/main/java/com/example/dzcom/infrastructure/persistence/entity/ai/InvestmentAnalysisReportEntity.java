package com.example.dzcom.infrastructure.persistence.entity.ai;

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
 * 投资分析报告持久化实体。
 *
 * <p>对应数据库表 {@code aiw_investment_analysis_report}。报告保存分析提供方、
 * 模型版本快照、四类核心分析结果以及供前端绘图的结构化数据，确保结果可查询、
 * 可解释和可审计。</p>
 */
@Schema(description = "投资分析报告持久化实体")
@TableName("aiw_investment_analysis_report")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentAnalysisReportEntity {
    /** 报告业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "投资分析报告业务唯一标识")
    private String bizId;
    /** 单次分析请求的幂等和追踪标识。 */
    @Schema(description = "本次分析请求唯一标识")
    private String requestId;
    /** 实际执行分析的 Provider 编码。 */
    @Schema(description = "分析提供方编码", example = "LOCAL_RULE")
    private String providerCode;
    /** 本次分析使用的模型或规则版本编码。 */
    @Schema(description = "模型或规则版本编码", example = "local-rule-v1")
    private String modelCode;
    /** 本次报告覆盖的市场范围。 */
    @Schema(description = "市场范围，默认中国大陆", example = "CN_MAINLAND")
    private String marketScope;
    /** 被分析的投资主题稳定编码；全市场分析时可为空。 */
    @Schema(description = "投资主题稳定编码")
    private String themeCode;
    /** 被分析的投资主题展示名称。 */
    @Schema(description = "投资主题展示名称")
    private String themeName;
    /** 报告生成状态。 */
    @Schema(description = "报告状态：SUCCEEDED/FAILED", example = "SUCCEEDED")
    private String status;
    /** 报告可信等级，前端列表页可直接展示。 */
    @Schema(description = "报告可信等级：HIGH_CONFIDENCE/MEDIUM_CONFIDENCE/LOW_CONFIDENCE/UNUSABLE")
    private String confidenceLevel;
    /** 报告输入数据质量分，0-1。 */
    @Schema(description = "报告输入数据质量分，0-1")
    private BigDecimal dataQualityScore;
    /** 数据质量门禁 JSON，包含是否通过和降级原因。 */
    @Schema(description = "数据质量门禁 JSON 字符串")
    private String dataQualityGate;
    /** 投资数据和近期资讯的汇总 JSON。 */
    @Schema(description = "投资信息汇总 JSON 字符串")
    private String investmentSummary;
    /** 收益方向、新闻热度和回看窗口等趋势 JSON。 */
    @Schema(description = "趋势分析 JSON 字符串")
    private String trend;
    /** 参考配置动作与风险提示 JSON。 */
    @Schema(description = "投资方案 JSON 字符串")
    private String investmentPlan;
    /** 初始资金、模拟收益和期末资金 JSON。 */
    @Schema(description = "模拟收益 JSON 字符串")
    private String simulatedReturn;
    /** 前端绘制收益、动量、热度和新闻事件图表的数据。 */
    @Schema(description = "前端图表结构化数据 JSON 字符串")
    private String chartPayload;
    /** 脱敏后的分析输入和参数快照。 */
    @Schema(description = "脱敏后的提示词与输入快照 JSON 字符串")
    private String promptSnapshot;
    /** 脱敏后的模型对话快照。 */
    @Schema(description = "脱敏后的模型对话快照 JSON 字符串")
    private String chatSnapshot;
    /** 失败时保存的原因摘要；成功时为空。 */
    @Schema(description = "失败原因摘要，成功时为空")
    private String failureReason;
    /** 报告实际生成时间，北京时间。 */
    @Schema(description = "报告生成时间，北京时间")
    private LocalDateTime generatedAt;
    /** 数据创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
}
