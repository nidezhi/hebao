package com.example.dzcom.domain.repository.task;

import com.example.dzcom.domain.model.task.NewsArticle;

import java.time.LocalDateTime;
import java.util.List;

/** 投资资讯仓储端口。 */
public interface NewsArticleStore {
    /** 保存资讯；相同来源和外部 ID 重复采集时更新内容。 */
    NewsArticle save(NewsArticle article);

    /** 统计时间窗口内标题或摘要命中任一关键词的资讯数量。 */
    long countByKeywords(List<String> keywords, LocalDateTime from);
}
