package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.service.task.RealMarketDataClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 基于 AKTools/AKShare 和东方财富公开接口的确定性市场数据客户端。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HttpRealMarketDataClient implements RealMarketDataClient {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final String EAST_MONEY_SOURCE_URL = "https://www.eastmoney.com";
    private static final Pattern EAST_MONEY_NEWS_LINK = Pattern.compile(
        "<a[^>]+href=[\"'](https://finance\\.eastmoney\\.com/a/[^\"']+\\.html)[\"'][^>]*>(.*?)</a>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final ObjectMapper objectMapper;

    @Override
    public List<ProductPayload> products(MarketDataRequest request) {
        List<ProductPayload> result = new ArrayList<>();
        for (String code : request.productCodes()) {
            result.add(ProductPayload.builder()
                .productCode(code)
                .productName(isBlank(request.providerBaseUrl()) ? defaultProductName(code) : resolveNameFromSpot(request, code))
                .productType(productType(code))
                .marketCode(marketCode(code))
                .currency("CNY")
                .riskLevel("STOCK".equals(productType(code)) ? 4 : 3)
                .description("REAL_PRODUCT_UNIVERSE_SYNC:" + request.sourceCode())
                .sourceUrl(request.providerBaseUrl())
                .build());
        }
        if (isBlank(request.providerBaseUrl())) {
            log.warn(
                "真实产品池同步未配置providerBaseUrl，仅按任务授权代码清单建立产品主档: sourceCode={}, productCount={}",
                request.sourceCode(),
                result.size()
            );
        }
        return result;
    }

    @Override
    public List<QuotePayload> quotes(MarketDataRequest request) {
        if (isBlank(request.providerBaseUrl())) {
            log.info("真实行情同步使用东方财富公开行情兜底: sourceCode={}, productCount={}",
                request.sourceCode(), request.productCodes().size());
            return eastMoneyQuotes(request);
        }
        List<QuotePayload> result = new ArrayList<>();
        for (String code : request.productCodes()) {
            result.addAll(fetchQuoteHistory(request, code));
        }
        return result.stream()
            .filter(item -> item.closePrice() != null)
            .limit(request.maxItems())
            .toList();
    }

    @Override
    public List<NewsPayload> news(MarketDataRequest request) {
        if (isBlank(request.providerBaseUrl())) {
            log.info("真实资讯同步使用东方财富公开资讯兜底: sourceCode={}, keywordCount={}",
                request.sourceCode(), request.keywords().size());
            return eastMoneyNews(request);
        }
        String body = get(request.providerBaseUrl() + "/api/public/stock_news_em", request.timeoutSeconds());
        if (isBlank(body)) {
            return List.of();
        }
        JsonNode root = readTree(body);
        List<JsonNode> nodes = nodes(root);
        LocalDateTime minPublishTime = LocalDateTime.now(SHANGHAI).minusDays(Math.max(request.lookbackDays(), 1));
        return nodes.stream()
            .map(node -> newsPayload(node, request.keywords(), request.providerBaseUrl()))
            .filter(item -> item.title() != null && !item.title().isBlank())
            .filter(item -> item.publishTime() == null || !item.publishTime().isBefore(minPublishTime))
            .filter(item -> request.keywords().isEmpty() || !item.matchedKeywords().isEmpty())
            .limit(request.maxItems())
            .toList();
    }

    private String resolveNameFromSpot(MarketDataRequest request, String code) {
        List<JsonNode> rows = nodes(readTree(get(request.providerBaseUrl() + "/api/public/fund_etf_spot_em",
            request.timeoutSeconds())));
        for (JsonNode row : rows) {
            if (code.equals(firstText(row, "代码", "code", "symbol", "基金代码"))) {
                return firstNonBlank(firstText(row, "名称", "name", "基金简称"), code);
            }
        }
        rows = nodes(readTree(get(request.providerBaseUrl() + "/api/public/stock_zh_a_spot_em",
            request.timeoutSeconds())));
        for (JsonNode row : rows) {
            if (code.equals(firstText(row, "代码", "code", "symbol"))) {
                return firstNonBlank(firstText(row, "名称", "name"), code);
            }
        }
        return defaultProductName(code);
    }

    private List<QuotePayload> fetchQuoteHistory(MarketDataRequest request, String code) {
        String endpoint = "ETF".equals(productType(code)) ? "/api/public/fund_etf_hist_em" : "/api/public/stock_zh_a_hist";
        String url = request.providerBaseUrl() + endpoint
            + "?symbol=" + encode(code)
            + "&period=daily"
            + "&adjust=";
        String body = get(url, request.timeoutSeconds());
        if (isBlank(body)) {
            log.warn("真实行情源未返回内容: sourceCode={}, productCode={}, url={}", request.sourceCode(), code, url);
            return List.of();
        }
        List<JsonNode> rows = nodes(readTree(body));
        int fromIndex = Math.max(0, rows.size() - Math.max(request.lookbackDays(), 2));
        List<QuotePayload> result = new ArrayList<>();
        BigDecimal previous = null;
        for (JsonNode row : rows.subList(fromIndex, rows.size())) {
            BigDecimal close = decimal(row, "收盘", "close", "单位净值", "累计净值", "最新价");
            if (close == null) {
                continue;
            }
            result.add(QuotePayload.builder()
                .productCode(code)
                .marketCode(marketCode(code))
                .quoteTime(parseTime(firstText(row, "日期", "date", "净值日期")).withHour(15).withMinute(0).withSecond(0))
                .openPrice(decimal(row, "开盘", "open"))
                .highPrice(decimal(row, "最高", "high"))
                .lowPrice(decimal(row, "最低", "low"))
                .closePrice(close)
                .previousClosePrice(previous)
                .volume(decimal(row, "成交量", "volume"))
                .turnoverAmount(decimal(row, "成交额", "amount"))
                .sourceUrl(url)
                .build());
            previous = close;
        }
        return result;
    }

    private List<QuotePayload> eastMoneyQuotes(MarketDataRequest request) {
        List<QuotePayload> result = new ArrayList<>();
        for (String code : request.productCodes()) {
            result.addAll(eastMoneyQuoteHistory(request, code));
        }
        return result.stream()
            .filter(item -> item.closePrice() != null)
            .limit(request.maxItems())
            .toList();
    }

    private List<QuotePayload> eastMoneyQuoteHistory(MarketDataRequest request, String code) {
        String secId = eastMoneySecId(code);
        String url = "https://push2his.eastmoney.com/api/qt/stock/kline/get"
            + "?secid=" + encode(secId)
            + "&fields1=f1,f2,f3,f4,f5,f6"
            + "&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61"
            + "&klt=101&fqt=1&beg=0&end=20500101";
        String body = get(url, request.timeoutSeconds());
        JsonNode klines = readTree(body).path("data").path("klines");
        if (!klines.isArray() || klines.isEmpty()) {
            log.warn("东方财富行情未返回K线: sourceCode={}, productCode={}, secId={}",
                request.sourceCode(), code, secId);
            return List.of();
        }
        List<String> rows = new ArrayList<>();
        klines.forEach(node -> rows.add(node.asText("")));
        int fromIndex = Math.max(0, rows.size() - Math.max(request.lookbackDays(), 2));
        List<QuotePayload> result = new ArrayList<>();
        BigDecimal previous = null;
        for (String row : rows.subList(fromIndex, rows.size())) {
            String[] fields = row.split(",");
            if (fields.length < 7) {
                continue;
            }
            BigDecimal close = decimal(fields[2]);
            if (close == null) {
                continue;
            }
            result.add(QuotePayload.builder()
                .productCode(code)
                .marketCode(marketCode(code))
                .quoteTime(parseTime(fields[0]).withHour(15).withMinute(0).withSecond(0))
                .openPrice(decimal(fields[1]))
                .closePrice(close)
                .highPrice(decimal(fields[3]))
                .lowPrice(decimal(fields[4]))
                .volume(decimal(fields[5]))
                .turnoverAmount(decimal(fields[6]))
                .previousClosePrice(previous)
                .sourceUrl(url)
                .build());
            previous = close;
        }
        return result;
    }

    private List<NewsPayload> eastMoneyNews(MarketDataRequest request) {
        Map<String, NewsPayload> result = new LinkedHashMap<>();
        for (String url : List.of(
            "https://finance.eastmoney.com/a/czqyw.html",
            "https://finance.eastmoney.com/a/cgnjj.html"
        )) {
            String body = get(url, request.timeoutSeconds());
            if (isBlank(body)) {
                continue;
            }
            Matcher matcher = EAST_MONEY_NEWS_LINK.matcher(body);
            while (matcher.find() && result.size() < request.maxItems()) {
                String sourceUrl = matcher.group(1);
                String title = cleanHtml(matcher.group(2));
                if (title.isBlank()) {
                    continue;
                }
                List<String> matched = matchedKeywords(title, request.keywords());
                if (!request.keywords().isEmpty() && matched.isEmpty()) {
                    continue;
                }
                result.putIfAbsent(sourceUrl, NewsPayload.builder()
                    .externalId(sourceUrl)
                    .articleType("NEWS")
                    .title(title)
                    .summary(title)
                    .content(title)
                    .sourceUrl(sourceUrl)
                    .publishTime(eastMoneyPublishTime(sourceUrl))
                    .matchedKeywords(matched)
                    .build());
            }
        }
        return new ArrayList<>(result.values());
    }

    private NewsPayload newsPayload(JsonNode node, List<String> keywords, String baseUrl) {
        String title = firstText(node, "新闻标题", "title", "标题");
        String summary = firstText(node, "新闻内容", "summary", "摘要", "content");
        String url = firstText(node, "新闻链接", "url", "链接");
        String text = (firstNonBlank(title, "") + " " + firstNonBlank(summary, "")).toLowerCase(Locale.ROOT);
        List<String> matched = keywords.stream()
            .filter(keyword -> text.contains(keyword.toLowerCase(Locale.ROOT)))
            .toList();
        return NewsPayload.builder()
            .externalId(firstNonBlank(url, title))
            .articleType("NEWS")
            .title(title)
            .summary(summary)
            .content(summary)
            .sourceUrl(firstNonBlank(url, baseUrl))
            .publishTime(parseTime(firstText(node, "发布时间", "publishTime", "time", "日期")))
            .matchedKeywords(matched)
            .build();
    }

    private String get(String url, int timeoutSeconds) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .header("Accept", "application/json,text/plain,*/*")
                .header("User-Agent", "DZCOM-RealMarketDataCollector/1.0")
                .GET()
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            log.warn("真实数据源HTTP返回非成功状态: url={}, statusCode={}", url, response.statusCode());
            return "";
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("真实数据源HTTP请求被中断: url={}", url);
            return "";
        } catch (Exception exception) {
            log.warn("真实数据源HTTP请求失败: url={}, exceptionType={}, reason={}",
                url, exception.getClass().getSimpleName(), exception.getMessage());
            return "";
        }
    }

    private JsonNode readTree(String body) {
        if (isBlank(body)) {
            return objectMapper.missingNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception exception) {
            return objectMapper.missingNode();
        }
    }

    private List<JsonNode> nodes(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return List.of();
        }
        JsonNode data = root.has("data") ? root.get("data") : root;
        if (data.isArray()) {
            List<JsonNode> rows = new ArrayList<>();
            data.forEach(rows::add);
            return rows;
        }
        return List.of(data);
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull()) {
                String text = value.asText("").trim();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }

    private BigDecimal decimal(JsonNode node, String... fields) {
        String text = firstText(node, fields).replace(",", "");
        return decimal(text);
    }

    private BigDecimal decimal(String text) {
        if (text.isBlank() || "-".equals(text)) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String eastMoneySecId(String code) {
        return marketCode(code).equals("SSE") ? "1." + code : "0." + code;
    }

    private LocalDateTime eastMoneyPublishTime(String sourceUrl) {
        Matcher matcher = Pattern.compile("/a/(\\d{8})\\d+\\.html").matcher(sourceUrl);
        if (!matcher.find()) {
            return LocalDateTime.now(SHANGHAI);
        }
        return parseTime(matcher.group(1));
    }

    private List<String> matchedKeywords(String text, List<String> keywords) {
        String lower = text.toLowerCase(Locale.ROOT);
        return keywords.stream()
            .filter(keyword -> lower.contains(keyword.toLowerCase(Locale.ROOT)))
            .toList();
    }

    private String cleanHtml(String value) {
        return value == null ? "" : value
            .replaceAll("<[^>]+>", "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .trim();
    }

    private LocalDateTime parseTime(String value) {
        if (isBlank(value)) {
            return LocalDateTime.now(SHANGHAI);
        }
        String text = value.trim();
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(SHANGHAI).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // continue
        }
        for (DateTimeFormatter formatter : List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        )) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // continue
            }
        }
        for (DateTimeFormatter formatter : List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )) {
            try {
                return LocalDate.parse(text, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // continue
            }
        }
        return LocalDateTime.now(SHANGHAI);
    }

    private String marketCode(String code) {
        if (code.startsWith("51") || code.startsWith("58") || code.startsWith("60") || code.startsWith("68")) {
            return "SSE";
        }
        return "SZSE";
    }

    private String productType(String code) {
        return code.startsWith("15") || code.startsWith("51") || code.startsWith("58") ? "ETF" : "STOCK";
    }

    private String defaultProductName(String code) {
        return switch (code) {
            case "159819" -> "人工智能ETF";
            case "588000" -> "科创50ETF";
            case "515980" -> "人工智能ETF";
            case "512480" -> "半导体ETF";
            case "159995" -> "芯片ETF";
            case "688981" -> "中芯国际";
            case "518880" -> "黄金ETF";
            case "159934" -> "黄金ETF";
            default -> code;
        };
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
