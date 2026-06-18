package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新闻、公告和研报持久化实体。
 *
 * <p>来源编码和外部内容 ID 共同用于采集幂等，文本内容用于主题热度统计和投资分析。</p>
 */
@Schema(description = "投资资讯持久化实体")
@TableName("aiw_news_article")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsArticleEntity {
    /** 资讯业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "资讯业务唯一标识")
    private String bizId;
    /** 数据源侧原始内容 ID，用于重复采集去重。 */
    @Schema(description = "数据源侧外部内容标识")
    private String externalId;
    /** 内容分类。 */
    @Schema(description = "内容类型：NEWS/ANNOUNCEMENT/RESEARCH", example = "NEWS")
    private String articleType;
    /** 资讯标题。 */
    @Schema(description = "资讯标题")
    private String title;
    /** 清洗后的内容摘要。 */
    @Schema(description = "资讯摘要")
    private String summary;
    /** 资讯正文或清洗后的完整内容。 */
    @Schema(description = "资讯正文")
    private String content;
    /** 数据来源稳定编码。 */
    @Schema(description = "资讯来源编码", example = "CN_MAINLAND_NEWS")
    private String sourceCode;
    /** 数据源原文链接；兜底资讯可为空。 */
    @Schema(description = "资讯原文链接")
    private String sourceUrl;
    /** 内容语言编码。 */
    @Schema(description = "内容语言编码", example = "zh-CN")
    private String languageCode;
    /** 内容发布时间，北京时间。 */
    @Schema(description = "内容发布时间，北京时间")
    private LocalDateTime publishTime;
    /** 平台完成采集的时间，北京时间。 */
    @Schema(description = "平台采集时间，北京时间")
    private LocalDateTime collectedAt;
    /** 数据创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 逻辑删除标记，0 表示有效，1 表示删除。 */
    @Schema(description = "逻辑删除标记：0-有效，1-删除", example = "0")
    private int deleted;
}
