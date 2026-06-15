package com.example.dzcom.application.service.task;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 配置驱动任务参数解析测试。 */
class TaskParameterParserTest {

    /** 主题配置应保持顺序并标准化产品编码。 */
    @Test
    void shouldParseThemeDefinitions() {
        var themes = TaskParameterParser.themes(Map.of(
            "themes", "AI人工智能=aapl,nvda;黄金=gld"
        ));

        assertEquals(2, themes.size());
        assertEquals(java.util.List.of("AAPL", "NVDA"), themes.get("AI人工智能"));
        assertEquals(java.util.List.of("GLD"), themes.get("黄金"));
    }

    /** 非正数配置必须被拒绝。 */
    @Test
    void shouldRejectNonPositiveInteger() {
        assertThrows(IllegalArgumentException.class,
            () -> TaskParameterParser.positiveInt(Map.of("windowMinutes", "0"),
                "windowMinutes", 60));
    }
}
