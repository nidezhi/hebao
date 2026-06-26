package com.example.dzcom.application.dto.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/** AI 数据源发现结果视图。 */
@Builder
@Schema(description = "AI 数据源发现结果视图")
public record DataSourceDiscoveryView(
    @Schema(description = "场景编码")
    String scenarioCode,
    @Schema(description = "使用的模型编码")
    String modelCode,
    @Schema(description = "模型提供方")
    String providerCode,
    @Schema(description = "环境")
    String environment,
    @Schema(description = "市场范围")
    String marketScope,
    @Schema(description = "资产类别")
    String assetClass,
    @Schema(description = "数据类型")
    String dataTypes,
    @Schema(description = "主题关键词")
    String topicKeywords,
    @Schema(description = "模型挂靠配置")
    Map<String, Object> modelBindingConfig,
    @Schema(description = "本次发现使用的 Skill 编码")
    String skillCode,
    @Schema(description = "本次发现使用的 Skill 版本")
    String skillVersion,
    @Schema(description = "Skill 指令摘要")
    String skillInstruction,
    @Schema(description = "候选数据源")
    List<DataSourceDiscoveryCandidateView> candidates,
    @Schema(description = "审核策略")
    String reviewPolicy,
    @Schema(description = "Prompt 预览")
    String promptPreview
) {
}
