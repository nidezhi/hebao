package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.service.task.OfficialDisclosureClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** 基于 JDK HttpClient 的官方披露采集实现。 */
@Component
@RequiredArgsConstructor
public class HttpOfficialDisclosureClient implements OfficialDisclosureClient {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Pattern LINK_PATTERN = Pattern.compile(
        "<a\\s+[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private final ObjectMapper objectMapper;

    /**
     * 拉取端点数据并按 JSON/HTML 两类格式解析。
     *
     * @param request 端点采集请求
     * @return 标准化披露条目
     * @author dz
     * @date 2026-06-24
     */
    @Override
    public List<DisclosureItem> fetch(DisclosureFetchRequest request) {
        String body = send(request);
        List<DisclosureItem> items = "HTML".equalsIgnoreCase(request.responseFormat())
            ? parseHtml(body, request)
            : parseJson(body, request);
        return items.stream()
            .filter(item -> matchesKeywords(item, request.includeKeywords()))
            .limit(request.maxItems())
            .toList();
    }

    /** 发送 HTTP GET 请求。 */
    private String send(DisclosureFetchRequest request) {
        URI uri = URI.create(request.endpointUrl());
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(Math.max(request.timeoutSeconds(), 1)))
            .header("Accept", "application/json,text/html;q=0.9,*/*;q=0.8")
            .header("User-Agent", "DZCOM-Investment-Collector/1.0")
            .GET()
            .build();
        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(request.timeoutSeconds(), 1)))
                .build()
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("官方端点HTTP状态异常: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("官方端点采集被中断", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("官方端点采集失败: " + exception.getMessage(), exception);
        }
    }

    /** 解析 JSON 响应。 */
    List<DisclosureItem> parseJson(String body, DisclosureFetchRequest request) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode itemsNode = atPath(root, request.itemsPath());
            List<JsonNode> nodes = itemNodes(itemsNode);
            return nodes.stream()
                .map(node -> DisclosureItem.builder()
                    .externalId(textAt(node, request.externalIdPath()))
                    .title(textAt(node, request.titlePath()))
                    .summary(firstNonBlank(textAt(node, request.summaryPath()), textAt(node, request.contentPath())))
                    .content(textAt(node, request.contentPath()))
                    .url(resolveUrl(request.endpointUrl(), textAt(node, request.urlPath())))
                    .publishTime(parseTime(textAt(node, request.publishTimePath())))
                    .extraFields(extraFields(node, request.extraFieldPaths()))
                    .build())
                .filter(item -> item.title() != null && !item.title().isBlank())
                .toList();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("官方端点JSON解析失败", exception);
        }
    }

    /** 解析简单 HTML 链接列表。 */
    List<DisclosureItem> parseHtml(String body, DisclosureFetchRequest request) {
        List<DisclosureItem> items = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(body);
        while (matcher.find() && items.size() < request.maxItems()) {
            String url = resolveUrl(request.endpointUrl(), decodeHtml(matcher.group(1)));
            String title = decodeHtml(TAG_PATTERN.matcher(matcher.group(2)).replaceAll("")).trim();
            if (!title.isBlank()) {
                items.add(DisclosureItem.builder()
                    .externalId(url)
                    .title(title)
                    .summary(title)
                    .content(title)
                    .url(url)
                    .publishTime(null)
                    .extraFields(Map.of())
                    .build());
            }
        }
        return items;
    }

    /** 解析任务配置的额外字段路径。 */
    private Map<String, String> extraFields(JsonNode node, String mapping) {
        if (mapping == null || mapping.isBlank()) {
            return Map.of();
        }
        return Arrays.stream(mapping.split(";"))
            .map(String::trim)
            .filter(item -> item.contains("="))
            .map(item -> item.split("=", 2))
            .collect(Collectors.toMap(
                item -> item[0].trim(),
                item -> textAt(node, item[1].trim()),
                (left, right) -> right,
                LinkedHashMap::new
            ));
    }

    /** 根据路径读取 JSON 节点。 */
    private JsonNode atPath(JsonNode root, String path) {
        if (path == null || path.isBlank()) {
            return root;
        }
        JsonNode current = root;
        for (String segment : path.split("\\.")) {
            if (current == null || current.isMissingNode()) {
                return objectMapper.missingNode();
            }
            current = current.path(segment);
        }
        return current;
    }

    /** 将 items 节点转换为条目节点集合。 */
    private List<JsonNode> itemNodes(JsonNode itemsNode) {
        if (itemsNode == null || itemsNode.isMissingNode() || itemsNode.isNull()) {
            return List.of();
        }
        if (itemsNode.isArray()) {
            List<JsonNode> nodes = new ArrayList<>();
            itemsNode.forEach(nodes::add);
            return nodes;
        }
        return List.of(itemsNode);
    }

    /** 读取字段文本。 */
    private String textAt(JsonNode node, String path) {
        JsonNode value = atPath(node, path);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return "";
        }
        return value.isValueNode() ? value.asText("").trim() : value.toString();
    }

    /** 解析发布时间。 */
    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(SHANGHAI).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // 尝试常见无时区格式。
        }
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // 继续尝试下一个格式。
            }
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(text, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // 继续尝试下一个格式。
            }
        }
        return null;
    }

    /** 根据端点地址解析相对链接。 */
    private String resolveUrl(String endpointUrl, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return URI.create(endpointUrl).resolve(value.trim()).toString();
    }

    /** 关键词过滤。 */
    private boolean matchesKeywords(DisclosureItem item, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        String text = (firstNonBlank(item.title(), "") + " "
            + firstNonBlank(item.summary(), "") + " "
            + firstNonBlank(item.content(), "")).toLowerCase(Locale.ROOT);
        return keywords.stream()
            .map(keyword -> keyword.toLowerCase(Locale.ROOT))
            .anyMatch(text::contains);
    }

    /** 获取第一个非空文本。 */
    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    /** 处理 HTML 常见实体。 */
    private String decodeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'");
    }
}
