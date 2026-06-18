package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 供热度汇总和投资分析使用的新闻、公告或研报领域对象。 */
@Builder
@Schema(description = "投资资讯领域对象")
public record NewsArticle(
    @Schema(description = "资讯业务唯一标识")
    String bizId,
    @Schema(description = "数据源侧外部内容标识")
    String externalId,
    @Schema(description = "内容类型")
    String articleType,
    @Schema(description = "资讯标题")
    String title,
    @Schema(description = "资讯摘要")
    String summary,
    @Schema(description = "资讯正文")
    String content,
    @Schema(description = "资讯来源编码")
    String sourceCode,
    @Schema(description = "资讯原文链接")
    String sourceUrl,
    @Schema(description = "内容语言编码")
    String languageCode,
    @Schema(description = "内容发布时间，北京时间")
    LocalDateTime publishTime,
    @Schema(description = "平台采集时间，北京时间")
    LocalDateTime collectedAt,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt
) {
}
