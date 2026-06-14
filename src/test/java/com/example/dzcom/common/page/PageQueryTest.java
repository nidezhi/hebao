package com.example.dzcom.common.page;

import com.example.dzcom.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 公共分页入参兼容和安全边界测试。 */
class PageQueryTest {
    @Test
    void shouldNormalizeZeroBasedFirstPage() {
        PageQuery query = new PageQuery(0, 20, "createdAt", "desc");

        assertEquals(1, query.page());
    }

    @Test
    void shouldKeepExistingOneBasedPageNumber() {
        PageQuery query = new PageQuery(2, 20, "createdAt", "asc");

        assertEquals(2, query.page());
        assertEquals("asc", query.direction());
    }

    @Test
    void shouldRejectNegativePageNumber() {
        assertThrows(BusinessException.class,
            () -> new PageQuery(-1, 20, "createdAt", "desc"));
    }

    @Test
    void shouldFallbackToAllowedSortField() {
        PageQuery query = new PageQuery(1, 20, "unknown", "desc");

        assertEquals("createdAt", query.safeSort(Set.of("createdAt"), "createdAt"));
    }
}
