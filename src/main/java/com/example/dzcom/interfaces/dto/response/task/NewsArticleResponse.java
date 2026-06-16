package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.domain.model.task.NewsArticle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资资讯响应。 */
@Builder
@Schema(description = "投资资讯响应")
public record NewsArticleResponse(
    @Schema(description = "资讯业务 ID") String bizId,
    @Schema(description = "外部资讯 ID") String externalId,
    @Schema(description = "资讯类型", example = "NEWS") String articleType,
    @Schema(description = "标题") String title,
    @Schema(description = "摘要") String summary,
    @Schema(description = "正文或清洗后的内容") String content,
    @Schema(description = "来源编码", example = "OFFICIAL_RSS") String sourceCode,
    @Schema(description = "原文链接") String sourceUrl,
    @Schema(description = "语言编码", example = "en-US") String languageCode,
    @Schema(description = "发布时间") LocalDateTime publishTime,
    @Schema(description = "采集时间") LocalDateTime collectedAt,
    @Schema(description = "创建时间") LocalDateTime createdAt
) {
    /** 将领域对象转换为接口响应。 */
    public static NewsArticleResponse from(NewsArticle article) {
        return NewsArticleResponse.builder()
            .bizId(article.bizId())
            .externalId(article.externalId())
            .articleType(article.articleType())
            .title(article.title())
            .summary(article.summary())
            .content(article.content())
            .sourceCode(article.sourceCode())
            .sourceUrl(article.sourceUrl())
            .languageCode(article.languageCode())
            .publishTime(article.publishTime())
            .collectedAt(article.collectedAt())
            .createdAt(article.createdAt())
            .build();
    }
}
