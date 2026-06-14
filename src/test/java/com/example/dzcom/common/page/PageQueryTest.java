package com.example.dzcom.common.page;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 公共分页入参兼容和安全边界测试。 */
class PageQueryTest {
    /**
     * 执行 should normalize zero based first page 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void shouldNormalizeZeroBasedFirstPage() {
        PageQuery query = new PageQuery(0, 20, "createdAt", "desc");

        assertEquals(1, query.page());
    }

    /**
     * 执行 should keep existing one based page number 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void shouldKeepExistingOneBasedPageNumber() {
        PageQuery query = new PageQuery(2, 20, "createdAt", "asc");

        assertEquals(2, query.page());
        assertEquals("asc", query.direction());
    }

    /**
     * 执行 should reject negative page number 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void shouldRejectNegativePageNumber() {
        assertThrows(BusinessException.class,
            () -> new PageQuery(-1, 20, "createdAt", "desc"));
    }

    /**
     * 执行 should fallback to allowed sort field 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void shouldFallbackToAllowedSortField() {
        PageQuery query = new PageQuery(1, 20, "unknown", "desc");

        assertEquals("createdAt", query.safeSort(Set.of("createdAt"), "createdAt"));
    }
}
