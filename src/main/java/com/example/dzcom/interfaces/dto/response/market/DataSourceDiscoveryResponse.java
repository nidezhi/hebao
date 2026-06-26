package com.example.dzcom.interfaces.dto.response.market;

import com.example.dzcom.application.dto.market.DataSourceDiscoveryView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/** AI 数据源发现响应。 */
@Builder
@Schema(description = "AI 数据源发现响应")
public record DataSourceDiscoveryResponse(
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
    @Schema(description = "采集方向")
    String collectionDirection,
    @Schema(description = "模型挂靠配置")
    Map<String, Object> modelBindingConfig,
    @Schema(description = "本次发现使用的 Skill 编码")
    String skillCode,
    @Schema(description = "本次发现使用的 Skill 版本")
    String skillVersion,
    @Schema(description = "Skill 指令摘要")
    String skillInstruction,
    @Schema(description = "候选数据源")
    List<DataSourceDiscoveryCandidateResponse> candidates,
    @Schema(description = "审核策略")
    String reviewPolicy,
    @Schema(description = "Prompt 预览")
    String promptPreview
) {
    /** 转换应用层发现结果。 */
    public static DataSourceDiscoveryResponse from(DataSourceDiscoveryView view) {
        return DataSourceDiscoveryResponse.builder()
            .scenarioCode(view.scenarioCode())
            .modelCode(view.modelCode())
            .providerCode(view.providerCode())
            .environment(view.environment())
            .marketScope(view.marketScope())
            .assetClass(view.assetClass())
            .dataTypes(view.dataTypes())
            .topicKeywords(view.topicKeywords())
            .collectionDirection(view.collectionDirection())
            .modelBindingConfig(view.modelBindingConfig())
            .skillCode(view.skillCode())
            .skillVersion(view.skillVersion())
            .skillInstruction(view.skillInstruction())
            .candidates(view.candidates().stream().map(DataSourceDiscoveryCandidateResponse::from).toList())
            .reviewPolicy(view.reviewPolicy())
            .promptPreview(view.promptPreview())
            .build();
    }
}
