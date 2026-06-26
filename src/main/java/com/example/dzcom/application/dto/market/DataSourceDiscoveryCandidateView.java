package com.example.dzcom.application.dto.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** AI 数据源发现候选视图。 */
@Builder
@Schema(description = "AI 数据源发现候选视图")
public record DataSourceDiscoveryCandidateView(
    @Schema(description = "建议数据源编码")
    String sourceCode,
    @Schema(description = "建议数据源名称")
    String sourceName,
    @Schema(description = "数据源类型")
    String sourceType,
    @Schema(description = "来源等级")
    String trustLevel,
    @Schema(description = "入口地址")
    String baseUrl,
    @Schema(description = "建议采集频率")
    String fetchFrequency,
    @Schema(description = "建议负责人或维护方")
    String owner,
    @Schema(description = "说明")
    String description,
    @Schema(description = "推荐任务类型")
    String recommendedTaskType,
    @Schema(description = "建议任务参数")
    Map<String, String> suggestedParameters,
    @Schema(description = "建议字段映射")
    Map<String, String> fieldMappings,
    @Schema(description = "AI 整理出的采集计划、接口说明、限制和样例")
    String collectionPlan,
    @Schema(description = "AI 整理出的质量校验规则")
    String qualityPolicy,
    @Schema(description = "候选置信度")
    BigDecimal confidence,
    @Schema(description = "推荐理由")
    List<String> reasons,
    @Schema(description = "是否需要人工审核")
    boolean requiresReview
) {
}
