package com.example.dzcom.application.service.task;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/** 外部投资资讯源访问端口。 */
public interface InvestmentNewsFeedClient {
    /** 拉取单个 RSS/Atom 地址中的资讯。 */
    List<FeedItem> fetch(String feedUrl, int maxItems);

    /** 外部资讯源标准条目。 */
    @Builder
    record FeedItem(
        String externalId,
        String title,
        String summary,
        String content,
        String url,
        LocalDateTime publishTime
    ) {
    }
}
