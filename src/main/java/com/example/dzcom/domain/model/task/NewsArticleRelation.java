package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 资讯与投资主题、产品之间的显式关联领域对象。 */
@Builder
@Schema(description = "资讯与投资主题、产品之间的显式关联领域对象")
public record NewsArticleRelation(
    @Schema(description = "关联记录业务唯一标识")
    String bizId,
    @Schema(description = "资讯业务唯一标识")
    String articleBizId,
    @Schema(description = "投资主题稳定编码")
    String themeCode,
    @Schema(description = "投资主题展示名称")
    String themeName,
    @Schema(description = "关联产品代码，可为空")
    String productCode,
    @Schema(description = "关联类型：KEYWORD_MATCH/MANUAL/MODEL_EXTRACTED")
    String relationType,
    @Schema(description = "命中的关键词集合")
    List<String> matchedKeywords,
    @Schema(description = "数据源质量分")
    BigDecimal sourceQualityScore,
    @Schema(description = "综合关联分")
    BigDecimal relationScore,
    @Schema(description = "关联证据摘要")
    String evidence,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt
) {
}
