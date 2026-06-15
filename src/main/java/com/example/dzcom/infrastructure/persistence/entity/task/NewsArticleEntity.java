package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 新闻资讯持久化实体。 */
@TableName("aiw_news_article")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsArticleEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String externalId;
    private String articleType;
    private String title;
    private String summary;
    private String content;
    private String sourceCode;
    private String sourceUrl;
    private String languageCode;
    private LocalDateTime publishTime;
    private LocalDateTime collectedAt;
    private LocalDateTime createdAt;
    private int deleted;
}
