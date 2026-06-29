package com.example.dzcom.interfaces.dto.response.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** 复盘闭环结构化元数据响应。 */
@Builder
@Schema(description = "复盘闭环结构化元数据响应，用于前端复盘表单的选择器、字典和字段级输入")
public record ReviewLoopMetadataResponse(
    @Schema(description = "回测策略选项")
    List<StrategyOptionResponse> strategies,
    @Schema(description = "基准选项")
    List<OptionResponse> benchmarkOptions,
    @Schema(description = "反馈原因码选项")
    List<OptionResponse> feedbackReasonOptions,
    @Schema(description = "Prompt评估场景选项")
    List<OptionResponse> evaluationScenarioOptions,
    @Schema(description = "从组合生成回测的参数字段")
    List<FieldSchemaResponse> generateParameterFields,
    @Schema(description = "手工保存回测的参数字段")
    List<FieldSchemaResponse> backtestParameterFields,
    @Schema(description = "手工保存回测的指标字段")
    List<FieldSchemaResponse> backtestMetricFields,
    @Schema(description = "投资反馈元数据字段")
    List<FieldSchemaResponse> feedbackMetadataFields,
    @Schema(description = "Prompt评估评分详情字段")
    List<FieldSchemaResponse> evaluationMetricFields
) {

    /** 默认复盘闭环元数据。 */
    public static ReviewLoopMetadataResponse defaults() {
        List<OptionResponse> benchmarks = List.of(
            option("CSI300", "沪深300", "A股核心宽基基准"),
            option("CSI500", "中证500", "中盘成长对照基准"),
            option("NONE", "不设置基准", "仅评估组合自身表现")
        );
        return ReviewLoopMetadataResponse.builder()
            .strategies(List.of(
                StrategyOptionResponse.builder()
                    .strategyCode("AUTO_CLOSED_LOOP_MOCK")
                    .strategyVersion("manual-v1")
                    .displayName("报告闭环模拟策略")
                    .description("从投资报告和Mock组合沉淀复盘样本的默认策略")
                    .defaultBenchmarkCode("CSI300")
                    .defaultLimit(100)
                    .defaultInitialCapital(new BigDecimal("1000000"))
                    .build(),
                StrategyOptionResponse.builder()
                    .strategyCode("REPORT_REVIEW_BASELINE")
                    .strategyVersion("v1")
                    .displayName("报告复核基线策略")
                    .description("用于人工保存回测结果时标记报告复核基线")
                    .defaultBenchmarkCode("CSI300")
                    .defaultLimit(100)
                    .defaultInitialCapital(new BigDecimal("1000000"))
                    .build()
            ))
            .benchmarkOptions(benchmarks)
            .feedbackReasonOptions(List.of(
                option("MANUAL_REVIEW", "人工复核", "人工复盘后录入"),
                option("AUTO_MOCK_EXECUTED", "模拟交易已执行", "Mock交易链路产生反馈"),
                option("QUALITY_GATE_REVIEW", "质量门禁复核", "报告或数据质量触发复核"),
                option("RISK_REVIEW", "风控复核", "风控规则提示需人工关注"),
                option("BACKTEST_REVIEW", "回测复核", "回测表现触发反馈")
            ))
            .evaluationScenarioOptions(List.of(
                option("MANUAL_REVIEW", "人工评估", "人工对Prompt版本进行评分"),
                option("BACKTEST_REVIEW", "回测反哺", "根据回测结果评价Prompt"),
                option("FEEDBACK_REVIEW", "反馈反哺", "根据用户反馈评价Prompt"),
                option("CLOSED_LOOP_REVIEW", "闭环复盘", "完整投资闭环后的综合评价")
            ))
            .generateParameterFields(List.of(
                field("source", "来源", "text", true, "REVIEW_LOOP", "参数来源标识"),
                field("reviewer", "复盘人", "text", false, "", "执行复盘的人员或系统"),
                field("note", "备注", "textarea", false, "", "生成回测时的复盘说明")
            ))
            .backtestParameterFields(List.of(
                field("source", "来源", "text", true, "REVIEW_LOOP_MANUAL", "参数来源标识"),
                field("portfolioBizId", "组合 BizId", "text", false, "", "由组合选择器自动带入"),
                field("reviewer", "复盘人", "text", false, "", "执行复盘的人员或系统"),
                field("note", "备注", "textarea", false, "", "保存回测时的复盘说明")
            ))
            .backtestMetricFields(List.of(
                field("returnRate", "收益率", "number", true, "0", "区间收益率，范围建议 -1 到 1"),
                field("maxDrawdown", "最大回撤", "number", true, "0", "最大回撤，使用正数表示回撤幅度"),
                field("volatility", "波动率", "number", true, "0", "收益波动率"),
                field("sharpeRatio", "夏普比率", "number", false, "0", "风险调整后收益指标"),
                field("note", "指标说明", "textarea", false, "", "指标口径或异常说明")
            ))
            .feedbackMetadataFields(List.of(
                field("source", "来源", "text", true, "REVIEW_LOOP", "反馈来源标识"),
                field("reviewer", "复盘人", "text", false, "", "反馈录入人"),
                field("confidence", "置信度", "number", false, "0.8", "反馈可信度，范围 0 到 1"),
                field("evidence", "依据", "textarea", false, "", "反馈依据摘要")
            ))
            .evaluationMetricFields(List.of(
                field("accuracy", "准确性", "number", true, "0.8", "结论与事实匹配程度，范围 0 到 1"),
                field("actionability", "可执行性", "number", true, "0.8", "建议是否可执行，范围 0 到 1"),
                field("riskAwareness", "风险意识", "number", true, "0.8", "风险披露充分性，范围 0 到 1"),
                field("reason", "评分理由", "textarea", false, "", "人工或系统评分说明")
            ))
            .build();
    }

    private static OptionResponse option(String value, String label, String description) {
        return OptionResponse.builder()
            .value(value)
            .label(label)
            .description(description)
            .build();
    }

    private static FieldSchemaResponse field(
        String fieldKey,
        String label,
        String valueType,
        boolean required,
        String defaultValue,
        String description
    ) {
        return FieldSchemaResponse.builder()
            .fieldKey(fieldKey)
            .label(label)
            .valueType(valueType)
            .required(required)
            .defaultValue(defaultValue)
            .description(description)
            .build();
    }

    /** 通用选项响应。 */
    @Builder
    @Schema(description = "复盘闭环通用选项")
    public record OptionResponse(
        @Schema(description = "选项值") String value,
        @Schema(description = "展示标签") String label,
        @Schema(description = "选项说明") String description
    ) {
    }

    /** 回测策略选项响应。 */
    @Builder
    @Schema(description = "复盘闭环回测策略选项")
    public record StrategyOptionResponse(
        @Schema(description = "策略稳定编码") String strategyCode,
        @Schema(description = "策略版本") String strategyVersion,
        @Schema(description = "展示名称") String displayName,
        @Schema(description = "策略说明") String description,
        @Schema(description = "默认基准编码") String defaultBenchmarkCode,
        @Schema(description = "默认样本上限") Integer defaultLimit,
        @Schema(description = "默认初始资金") BigDecimal defaultInitialCapital
    ) {
    }

    /** 复盘闭环字段 schema 响应。 */
    @Builder
    @Schema(description = "复盘闭环字段 schema")
    public record FieldSchemaResponse(
        @Schema(description = "字段键") String fieldKey,
        @Schema(description = "展示名称") String label,
        @Schema(description = "值类型：text/textarea/number/date") String valueType,
        @Schema(description = "是否必填") Boolean required,
        @Schema(description = "默认值") String defaultValue,
        @Schema(description = "字段说明") String description
    ) {
    }
}
