package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.domain.model.task.NewsArticleRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 资讯主题产品关联响应。 */
@Builder
@Schema(description = "资讯主题产品关联响应，供前端解释新闻热度来源和产品关联证据")
public record NewsArticleRelationResponse(
    @Schema(description = "关联记录业务 ID")
    String bizId,
    @Schema(description = "资讯业务 ID，可用于回查 /api/investment/tasks/articles/list 返回的新闻")
    String articleBizId,
    @Schema(description = "投资主题稳定编码")
    String themeCode,
    @Schema(description = "投资主题展示名称")
    String themeName,
    @Schema(description = "关联产品代码；空字符串表示主题级关联，不绑定具体产品")
    String productCode,
    @Schema(description = "关联类型，允许值：KEYWORD_MATCH/MANUAL/MODEL_EXTRACTED")
    String relationType,
    @Schema(description = "命中的关键词数组；例如 [\"AI\", \"人工智能\", \"算力\"]")
    List<String> matchedKeywords,
    @Schema(description = "数据源质量分，0-1；监管、交易所和可信来源更高，兜底资讯更低")
    BigDecimal sourceQualityScore,
    @Schema(description = "综合关联分，按关键词命中数、来源质量和时效计算；越高表示该新闻越能解释主题热度")
    BigDecimal relationScore,
    @Schema(description = "关联证据摘要，当前主要保存新闻标题")
    String evidence,
    @Schema(description = "关联创建时间，北京时间")
    LocalDateTime createdAt
) {

    /**
     * 将领域对象转换为接口响应。
     *
     * @param relation 资讯主题产品关联领域对象
     * @return 前端可展示的关联响应
     * @author dz
     * @date 2026-06-21
     */
    public static NewsArticleRelationResponse from(NewsArticleRelation relation) {
        return NewsArticleRelationResponse.builder()
            .bizId(relation.bizId())
            .articleBizId(relation.articleBizId())
            .themeCode(relation.themeCode())
            .themeName(relation.themeName())
            .productCode(relation.productCode())
            .relationType(relation.relationType())
            .matchedKeywords(relation.matchedKeywords())
            .sourceQualityScore(relation.sourceQualityScore())
            .relationScore(relation.relationScore())
            .evidence(relation.evidence())
            .createdAt(relation.createdAt())
            .build();
    }
}
