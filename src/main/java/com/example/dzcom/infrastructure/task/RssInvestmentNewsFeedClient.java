package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.service.task.InvestmentNewsFeedClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.IntStream;

/** 基于标准 XML 解析器的 RSS/Atom 投资资讯客户端。 */
@Component
public class RssInvestmentNewsFeedClient implements InvestmentNewsFeedClient {
    private final RestClient restClient = RestClient.builder()
        .defaultHeader("User-Agent", "dzcom-investment-research/1.0 contact@example.com")
        .build();

    /** 拉取并解析 RSS 或 Atom 资讯源。 */
    @Override
    public List<FeedItem> fetch(String feedUrl, int maxItems) {
        String xml = restClient.get().uri(feedUrl).retrieve().body(String.class);
        if (xml == null || xml.isBlank()) {
            return List.of();
        }
        Document document = parse(xml);
        NodeList rssItems = document.getElementsByTagName("item");
        NodeList nodes = rssItems.getLength() > 0
            ? rssItems
            : document.getElementsByTagNameNS("*", "entry");
        return IntStream.range(0, Math.min(nodes.getLength(), maxItems))
            .mapToObj(nodes::item)
            .filter(node -> node instanceof Element)
            .map(node -> toFeedItem((Element) node))
            .filter(item -> item.title() != null && !item.title().isBlank())
            .toList();
    }

    /** 安全解析 XML，禁用外部实体。 */
    private Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (Exception exception) {
            throw new IllegalStateException("RSS/Atom 资讯解析失败", exception);
        }
    }

    /** 将 RSS/Atom 节点转换为标准资讯条目。 */
    private FeedItem toFeedItem(Element element) {
        String title = text(element, "title");
        String url = firstNonBlank(text(element, "link"), attribute(element, "link", "href"));
        String externalId = firstNonBlank(
            text(element, "guid"),
            text(element, "id"),
            digest(title + "|" + url)
        );
        String summary = firstNonBlank(
            text(element, "description"),
            text(element, "summary")
        );
        return FeedItem.builder()
            .externalId(externalId)
            .title(title)
            .summary(summary)
            .content(firstNonBlank(text(element, "encoded"), text(element, "content")))
            .url(url)
            .publishTime(parseTime(firstNonBlank(
                text(element, "pubDate"),
                text(element, "published"),
                text(element, "updated")
            )))
            .build();
    }

    /** 读取指定本地名称的首个子节点文本。 */
    private String text(Element element, String localName) {
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            nodes = element.getElementsByTagName(localName);
        }
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent().trim();
    }

    /** 读取指定子节点属性。 */
    private String attribute(Element element, String localName, String attributeName) {
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            nodes = element.getElementsByTagName(localName);
        }
        if (nodes.getLength() == 0 || !(nodes.item(0) instanceof Element child)) {
            return null;
        }
        return child.getAttribute(attributeName);
    }

    /** 解析常见 RSS 和 Atom 时间格式并统一为 UTC。 */
    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        List<java.util.function.Supplier<LocalDateTime>> parsers = List.of(
            () -> ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
            () -> OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime(),
            () -> LocalDateTime.parse(value)
        );
        return parsers.stream()
            .map(parser -> {
                try {
                    return parser.get();
                } catch (RuntimeException ignored) {
                    return null;
                }
            })
            .filter(parsed -> parsed != null)
            .findFirst()
            .orElse(null);
    }

    /** 返回首个非空字符串。 */
    private String firstNonBlank(String... values) {
        return java.util.Arrays.stream(values)
            .filter(value -> value != null && !value.isBlank())
            .findFirst()
            .orElse(null);
    }

    /** 对缺少稳定 ID 的条目生成确定性摘要。 */
    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("资讯摘要生成失败", exception);
        }
    }
}
