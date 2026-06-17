package com.example.dzcom.infrastructure.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** 系统业务时钟测试。 */
class SystemClockProviderTest {
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    /** 业务时钟应返回北京时间窗口内的当前时间。 */
    @Test
    void shouldReturnBeijingLocalDateTime() {
        LocalDateTime before = LocalDateTime.now(BEIJING_ZONE).minusSeconds(1);
        LocalDateTime actual = new SystemClockProvider().now();
        LocalDateTime after = LocalDateTime.now(BEIJING_ZONE).plusSeconds(1);

        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }
}
