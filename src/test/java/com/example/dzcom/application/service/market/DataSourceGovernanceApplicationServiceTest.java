package com.example.dzcom.application.service.market;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 数据源治理应用服务测试。 */
class DataSourceGovernanceApplicationServiceTest {
    @Test
    void shouldNormalizeLongFetchFrequencyFromModelOutput() throws Exception {
        DataSourceGovernanceApplicationService service = new DataSourceGovernanceApplicationService(
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            null,
            null
        );

        String normalized = normalizeFetchFrequency(service, """
            建议实时监听监管披露和行情变动，结合每日收盘后复核、每小时刷新热点新闻、
            每周进行质量复盘，采集限制包括 robots、接口限频、字段映射和失败重试策略。
            这段内容本质上应该进入 collectionPlan，而不是 fetchFrequency。
            """.repeat(5));

        assertEquals("REALTIME", normalized);
    }

    @Test
    void shouldKeepFetchFrequencyWithinColumnBoundary() throws Exception {
        DataSourceGovernanceApplicationService service = new DataSourceGovernanceApplicationService(
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            null,
            null
        );

        String normalized = normalizeFetchFrequency(service, "未知频率说明".repeat(100));

        assertTrue(normalized.length() <= 255);
    }

    @Test
    void shouldKeepAiWritableDataSourceFieldsWithinColumnBoundaries() throws Exception {
        DataSourceGovernanceApplicationService service = new DataSourceGovernanceApplicationService(
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            null,
            null
        );

        assertTrue(normalizeCode(service, "very-long-source-code-".repeat(20)).length() <= 64);
        assertTrue(limitText(service, "名称".repeat(100), 128).length() <= 128);
        assertTrue(limitText(service, "https://example.com/".repeat(100), 512).length() <= 512);
        assertTrue(normalizeOwner(service, "维护方说明".repeat(100)).length() <= 255);
        assertTrue(limitText(service, "说明".repeat(400), 512).length() <= 512);
    }

    private String normalizeFetchFrequency(DataSourceGovernanceApplicationService service, String value) throws Exception {
        Method method = DataSourceGovernanceApplicationService.class
            .getDeclaredMethod("normalizeFetchFrequency", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }

    private String normalizeCode(DataSourceGovernanceApplicationService service, String value) throws Exception {
        Method method = DataSourceGovernanceApplicationService.class
            .getDeclaredMethod("normalizeCode", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value, "数据源编码不能为空");
    }

    private String normalizeOwner(DataSourceGovernanceApplicationService service, String value) throws Exception {
        Method method = DataSourceGovernanceApplicationService.class
            .getDeclaredMethod("normalizeOwner", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }

    private String limitText(DataSourceGovernanceApplicationService service, String value, int maxLength) throws Exception {
        Method method = DataSourceGovernanceApplicationService.class
            .getDeclaredMethod("limitText", String.class, int.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value, maxLength);
    }
}
