package com.example.dzcom.application.service.task;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** 投资任务配置参数解析器。 */
public final class TaskParameterParser {
    private TaskParameterParser() {
    }

    /** 读取正整数参数。 */
    public static int positiveInt(Map<String, String> parameters, String key, int defaultValue) {
        String value = parameters.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        int parsed = Integer.parseInt(value.trim());
        if (parsed < 1) {
            throw new IllegalArgumentException(key + " 必须大于 0");
        }
        return parsed;
    }

    /** 按逗号解析非空字符串列表。 */
    public static List<String> list(Map<String, String> parameters, String key) {
        String value = parameters.getOrDefault(key, "");
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
    }

    /**
     * 解析“主题名称=值1,值2;主题名称2=值3”格式。
     *
     * @return 保持配置顺序的主题映射
     */
    public static Map<String, List<String>> themes(Map<String, String> parameters) {
        return Arrays.stream(parameters.getOrDefault("themes", "").split(";"))
            .map(String::trim)
            .filter(item -> item.contains("="))
            .map(item -> item.split("=", 2))
            .collect(Collectors.toMap(
                item -> item[0].trim(),
                item -> Arrays.stream(item[1].split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(value -> value.toUpperCase(Locale.ROOT))
                    .toList(),
                (left, right) -> right,
                LinkedHashMap::new
            ));
    }

    /** 将主题名称转换为稳定主题编码。 */
    public static String themeCode(String themeName) {
        return themeName.trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9\\p{IsHan}]+", "_");
    }
}
