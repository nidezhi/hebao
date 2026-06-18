package com.example.dzcom.domain.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.NewsArticle;

import java.time.LocalDateTime;
import java.util.List;

/** 投资资讯仓储端口。 */
public interface NewsArticleStore {
    /** 保存资讯；相同来源和外部 ID 重复采集时更新内容。 */
    NewsArticle save(NewsArticle article);

    /** 统计时间窗口内标题或摘要命中任一关键词的资讯数量。 */
    long countByKeywords(List<String> keywords, LocalDateTime from);

    /** 查询时间窗口内标题或摘要命中任一关键词的近期资讯。 */
    List<NewsArticle> findRecentByKeywords(List<String> keywords, LocalDateTime from, int limit);

    /** 根据筛选条件分页查询资讯。 */
    PageResult<NewsArticle> search(NewsArticleSearchCriteria criteria);
}
