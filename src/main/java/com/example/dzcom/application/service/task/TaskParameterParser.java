package com.example.dzcom.application.service.task;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** 投资任务配置参数解析器。 */
public final class TaskParameterParser {
    /** 中国大陆市场范围编码。 */
    public static final String CN_MAINLAND = "CN_MAINLAND";

    private TaskParameterParser() {
    }

    /** 读取正整数参数。 */
    public static int positiveInt(Map<String, String> parameters, String key, int defaultValue) {
        if (parameters == null) {
            return defaultValue;
        }
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
        if (parameters == null) {
            return List.of();
        }
        String value = parameters.getOrDefault(key, "");
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
    }

    /** 读取原始字符串参数。 */
    public static String string(Map<String, String> parameters, String key, String defaultValue) {
        if (parameters == null) {
            return defaultValue;
        }
        String value = parameters.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 读取布尔参数。
     *
     * @param parameters 任务参数集合
     * @param key 参数键
     * @param defaultValue 默认值
     * @return true/false；空值使用默认值
     * @author dz
     * @date 2026-06-24
     */
    public static boolean bool(Map<String, String> parameters, String key, boolean defaultValue) {
        if (parameters == null) {
            return defaultValue;
        }
        String value = parameters.get(key);
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    /**
     * 解析“主题名称=值1,值2;主题名称2=值3”格式。
     *
     * @return 保持配置顺序的主题映射
     */
    public static Map<String, List<String>> themes(Map<String, String> parameters) {
        return themes(parameters, "themes");
    }

    /**
     * 按指定参数键解析“主题名称=值1,值2;主题名称2=值3”格式。
     *
     * @param parameters 任务参数集合
     * @param key 参数键
     * @return 保持配置顺序的主题映射
     * @author dz
     * @date 2026-06-21
     */
    public static Map<String, List<String>> themes(Map<String, String> parameters, String key) {
        if (parameters == null) {
            return new LinkedHashMap<>();
        }
        return filterByMarketScope(Arrays.stream(parameters.getOrDefault(key, "").split(";"))
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
            )), parameters, marketScope(parameters));
    }

    /** 读取市场范围参数，默认仅中国大陆。 */
    public static String marketScope(Map<String, String> parameters) {
        if (parameters == null) {
            return CN_MAINLAND;
        }
        String value = parameters.getOrDefault("marketScope", CN_MAINLAND);
        return value == null || value.isBlank() ? CN_MAINLAND : value.trim().toUpperCase(Locale.ROOT);
    }

    /** 根据主题市场映射只保留指定市场范围的主题。 */
    private static Map<String, List<String>> filterByMarketScope(
        Map<String, List<String>> themes,
        Map<String, String> parameters,
        String marketScope
    ) {
        String mapping = parameters.getOrDefault("themeMarketScopes", "");
        if (marketScope == null || marketScope.isBlank()) {
            return themes;
        }
        return themes.entrySet().stream()
            .filter(entry -> themeMarketScopes(mapping).getOrDefault(entry.getKey(), CN_MAINLAND).equals(marketScope))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (left, right) -> right,
                LinkedHashMap::new
            ));
    }

    /** 解析“主题名称=CN_MAINLAND;主题名称2=US”格式的主题市场映射。 */
    private static Map<String, String> themeMarketScopes(String mapping) {
        return Arrays.stream(mapping.split(";"))
            .map(String::trim)
            .filter(item -> item.contains("="))
            .map(item -> item.split("=", 2))
            .collect(Collectors.toMap(
                item -> item[0].trim(),
                item -> item[1].trim().toUpperCase(Locale.ROOT),
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
