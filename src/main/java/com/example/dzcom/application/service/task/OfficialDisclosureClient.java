package com.example.dzcom.application.service.task;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 官方披露、公告和理财净值源访问端口。 */
public interface OfficialDisclosureClient {
    /**
     * 从一个官方或授权端点拉取结构化披露条目。
     *
     * @param request 端点、响应格式、字段映射和过滤条件
     * @return 标准化披露条目
     * @author dz
     * @date 2026-06-24
     */
    List<DisclosureItem> fetch(DisclosureFetchRequest request);

    /** 官方披露端点请求。 */
    @Builder
    record DisclosureFetchRequest(
        String endpointName,
        String endpointUrl,
        String responseFormat,
        String itemsPath,
        String externalIdPath,
        String titlePath,
        String summaryPath,
        String contentPath,
        String urlPath,
        String publishTimePath,
        String extraFieldPaths,
        List<String> includeKeywords,
        int maxItems,
        int timeoutSeconds
    ) {
    }

    /** 标准化官方披露条目。 */
    @Builder
    record DisclosureItem(
        String externalId,
        String title,
        String summary,
        String content,
        String url,
        LocalDateTime publishTime,
        Map<String, String> extraFields
    ) {
    }
}
