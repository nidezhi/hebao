package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.service.task.OfficialDisclosureClient;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 官方披露 HTTP 客户端测试。 */
class HttpOfficialDisclosureClientTest {

    /** 客户端应按配置路径解析 JSON 列表。 */
    @Test
    void shouldParseConfiguredJsonPaths() {
        HttpOfficialDisclosureClient client =
            new HttpOfficialDisclosureClient(JsonMapper.builder().findAndAddModules().build());
        String body = """
            {
              "data": {
                "items": [
                  {
                    "noticeId": "n-1",
                    "noticeTitle": "人工智能公告",
                    "noticeSummary": "公告摘要",
                    "noticeUrl": "/notice/1",
                    "publishDate": "2026-06-24 09:30:00"
                  }
                ]
              }
            }
            """;
        List<OfficialDisclosureClient.DisclosureItem> items = client.parseJson(
            body,
            OfficialDisclosureClient.DisclosureFetchRequest.builder()
                .endpointName("test")
                .endpointUrl("https://example.com/api/notices")
                .responseFormat("JSON")
                .itemsPath("data.items")
                .externalIdPath("noticeId")
                .titlePath("noticeTitle")
                .summaryPath("noticeSummary")
                .contentPath("noticeSummary")
                    .urlPath("noticeUrl")
                    .publishTimePath("publishDate")
                    .extraFieldPaths("productCode=noticeId;nav=nav")
                    .includeKeywords(List.of("人工智能"))
                    .maxItems(10)
                    .timeoutSeconds(5)
                .build()
        );

        assertEquals(1, items.size());
        assertEquals("n-1", items.get(0).externalId());
        assertEquals("人工智能公告", items.get(0).title());
        assertEquals(LocalDateTime.of(2026, 6, 24, 9, 30), items.get(0).publishTime());
        assertEquals("n-1", items.get(0).extraFields().get("productCode"));
    }
}
