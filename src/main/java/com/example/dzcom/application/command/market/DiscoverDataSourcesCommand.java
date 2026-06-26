package com.example.dzcom.application.command.market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** AI 数据源发现命令。 */
@Builder
@Schema(description = "AI 数据源发现命令")
public record DiscoverDataSourcesCommand(
    @Schema(description = "市场范围")
    String marketScope,
    @Schema(description = "资产类别")
    String assetClass,
    @Schema(description = "目标数据类型，逗号分隔")
    String dataTypes,
    @Schema(description = "主题关键词，逗号分隔")
    String topicKeywords,
    @Schema(description = "偏好的来源等级，逗号分隔")
    String preferredTrustLevels,
    @Schema(description = "候选数量上限")
    Integer candidateLimit,
    @Schema(description = "模型挂靠环境")
    String environment,
    @Schema(description = "是否包含需要授权或暂不可用的候选")
    Boolean includeDisabledCandidates
) {
}
