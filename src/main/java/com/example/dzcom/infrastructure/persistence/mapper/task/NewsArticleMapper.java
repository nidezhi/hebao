package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
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

    /** 查询指定时间后命中任一关键词的近期资讯。 */
    List<NewsArticleEntity> findRecentByKeywords(
        @Param("keywords") List<String> keywords,
        @Param("from") LocalDateTime from,
        @Param("limit") int limit
    );

    /** 根据筛选条件分页查询资讯。 */
    List<NewsArticleEntity> search(@Param("criteria") NewsArticleSearchCriteria criteria,
                                   @Param("offset") int offset,
                                   @Param("sortColumn") String sortColumn);

    /** 统计符合筛选条件的资讯数量。 */
    long count(@Param("criteria") NewsArticleSearchCriteria criteria);
}
