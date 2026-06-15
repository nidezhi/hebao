package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.infrastructure.persistence.entity.task.NewsArticleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** 新闻资讯 MyBatis Mapper。 */
@Mapper
public interface NewsArticleMapper {
    /** 根据来源和外部 ID 查询资讯。 */
    NewsArticleEntity selectBySourceAndExternalId(
        @Param("sourceCode") String sourceCode,
        @Param("externalId") String externalId
    );

    /** 新增或更新资讯。 */
    int save(NewsArticleEntity entity);

    /** 统计指定时间后命中任一关键词的资讯数量。 */
    long countByKeywords(
        @Param("keywords") List<String> keywords,
        @Param("from") LocalDateTime from
    );
}
