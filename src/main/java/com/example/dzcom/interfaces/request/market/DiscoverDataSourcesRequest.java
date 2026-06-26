package com.example.dzcom.interfaces.request.market;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 数据源发现请求。 */
@Schema(description = "AI 数据源发现请求")
public record DiscoverDataSourcesRequest(
    @Schema(description = "市场范围，默认 CN_MAINLAND")
    String marketScope,
    @Schema(description = "资产类别，例如 BANK_WMP/FUND/ETF/STOCK/MULTI_ASSET")
    String assetClass,
    @Schema(description = "目标数据类型，逗号分隔：MARKET_QUOTE/NEWS/ANNOUNCEMENT/RESEARCH/REGULATORY")
    String dataTypes,
    @Schema(description = "主题关键词，逗号分隔")
    String topicKeywords,
    @Schema(description = "采集方向，例如 OFFICIAL_DISCLOSURE/NEWS_RESEARCH/PRODUCT_NAV/MULTI_SOURCE")
    String collectionDirection,
    @Schema(description = "本次使用的 Skill 编码，空值按数据类型和采集方向自动选择")
    String skillCode,
    @Schema(description = "偏好的来源等级，逗号分隔：L1/L2/L3/L4/L5")
    String preferredTrustLevels,
    @Schema(description = "候选数量上限，默认取模型挂靠配置")
    Integer candidateLimit,
    @Schema(description = "模型挂靠配置环境，默认 DEFAULT")
    String environment,
    @Schema(description = "是否包含需要授权或暂不可用的候选")
    Boolean includeDisabledCandidates
) {
}
