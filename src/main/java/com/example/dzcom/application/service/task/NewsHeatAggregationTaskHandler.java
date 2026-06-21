package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.NewsArticleRelation;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleRelationStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 投资资讯主题热度汇总任务。 */
@Service
@RequiredArgsConstructor
public class NewsHeatAggregationTaskHandler implements InvestmentTaskHandler {
    private static final BigDecimal MAX_SOURCE_QUALITY_SCORE = BigDecimal.valueOf(1);
    private static final BigDecimal TRUSTED_SOURCE_SCORE = BigDecimal.valueOf(1);
    private static final BigDecimal NORMAL_SOURCE_SCORE = BigDecimal.valueOf(0.7);
    private static final BigDecimal FALLBACK_SOURCE_SCORE = BigDecimal.valueOf(0.35);
    private static final Set<String> TRUSTED_SOURCE_CODES = Set.of(
        "CSRC",
        "SSE",
        "SZSE",
        "BSE",
        "EASTMONEY",
        "CNINFO",
        "XINHUA",
        "SECURITIES_TIMES",
        "CHINA_SECURITIES_JOURNAL",
        "CN_MAINLAND_NEWS"
    );

    private final NewsArticleStore articles;
    private final NewsArticleRelationStore relations;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 支持资讯热度汇总任务。 */
    @Override
    public boolean supports(String taskType) {
        return "NEWS_HEAT_AGGREGATION".equals(taskType);
    }

    /** 按主题关键词统计窗口内资讯数量并生成热度快照。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        int windowMinutes = TaskParameterParser.positiveInt(
            event.parameters(), "windowMinutes", 1440);
        String marketScope = TaskParameterParser.marketScope(event.parameters());
        LocalDateTime now = clock.now();
        Map<String, List<String>> themes = TaskParameterParser.themes(event.parameters());
        Map<String, List<String>> themeProducts = TaskParameterParser.themes(
            event.parameters(), "themeProducts");
        themes.forEach((themeName, keywords) ->
            aggregateTheme(event.taskCode(), themeName, keywords, themeProducts,
                marketScope, windowMinutes, now));
        return "已汇总 " + themes.size() + " 个投资方向的资讯热度";
    }

    /**
     * 汇总单个主题的资讯热度和显式关联。
     *
     * @param taskCode 来源任务编码
     * @param themeName 投资主题名称
     * @param keywords 主题关键词集合
     * @param themeProducts 主题与产品代码映射
     * @param marketScope 市场范围
     * @param windowMinutes 回看窗口分钟数
     * @param now 当前北京时间
     * @author dz
     * @date 2026-06-21
     */
    private void aggregateTheme(
        String taskCode,
        String themeName,
        List<String> keywords,
        Map<String, List<String>> themeProducts,
        String marketScope,
        int windowMinutes,
        LocalDateTime now
    ) {
        String themeCode = TaskParameterParser.themeCode(themeName);
        List<NewsArticle> matchedArticles = articles.findRecentByKeywords(
            keywords,
            now.minusMinutes(windowMinutes),
            200
        );
        NewsHeatMetrics metrics = calculateMetrics(matchedArticles, keywords, now);
        List<NewsArticleRelation> articleRelations = buildRelations(
            matchedArticles,
            themeCode,
            themeName,
            themeProducts.getOrDefault(themeName, List.of()),
            keywords,
            now
        );
        relations.saveBatch(articleRelations);
        snapshots.save(InvestmentThemeSnapshot.builder()
            .bizId(ids.newBizId())
            .taskCode(taskCode)
            .snapshotType("NEWS_HEAT")
            .themeCode(themeCode)
            .themeName(themeName)
            .marketScope(marketScope)
            .windowMinutes(windowMinutes)
            .sampleCount(matchedArticles.size())
            .heatScore(metrics.heatScore())
            .metrics(writeMetrics(themeName, keywords, metrics, matchedArticles))
            .snapshotTime(now)
            .createdAt(now)
            .build());
    }

    /**
     * 计算资讯热度质量指标。
     *
     * @param matchedArticles 关键词命中的资讯集合
     * @param keywords 主题关键词集合
     * @param now 当前北京时间
     * @return 资讯热度指标
     * @author dz
     * @date 2026-06-21
     */
    private NewsHeatMetrics calculateMetrics(
        List<NewsArticle> matchedArticles,
        List<String> keywords,
        LocalDateTime now
    ) {
        List<BigDecimal> relationScores = matchedArticles.stream()
            .map(article -> relationScore(article, keywords, now))
            .toList();
        BigDecimal heatScore = relationScores.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);
        long uniqueSourceCount = matchedArticles.stream()
            .map(NewsArticle::sourceCode)
            .distinct()
            .count();
        BigDecimal averageSourceQuality = matchedArticles.isEmpty()
            ? BigDecimal.ZERO
            : matchedArticles.stream()
                .map(article -> sourceQualityScore(article.sourceCode()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(matchedArticles.size()), 4, RoundingMode.HALF_UP);
        BigDecimal dataQualityScore = calculateDataQualityScore(
            matchedArticles.size(),
            uniqueSourceCount,
            averageSourceQuality
        );
        return NewsHeatMetrics.builder()
            .articleCount(matchedArticles.size())
            .uniqueSourceCount(uniqueSourceCount)
            .averageSourceQuality(averageSourceQuality)
            .dataQualityScore(dataQualityScore)
            .heatScore(heatScore)
            .qualityLevel(resolveQualityLevel(dataQualityScore))
            .build();
    }

    /**
     * 构建资讯、主题和产品显式关联集合。
     *
     * @param matchedArticles 命中的资讯集合
     * @param themeCode 投资主题编码
     * @param themeName 投资主题名称
     * @param productCodes 主题关联产品代码
     * @param keywords 主题关键词集合
     * @param now 当前北京时间
     * @return 可批量落库的关联集合
     * @author dz
     * @date 2026-06-21
     */
    private List<NewsArticleRelation> buildRelations(
        List<NewsArticle> matchedArticles,
        String themeCode,
        String themeName,
        List<String> productCodes,
        List<String> keywords,
        LocalDateTime now
    ) {
        List<String> normalizedProductCodes = productCodes.isEmpty()
            ? List.of("")
            : productCodes;
        return matchedArticles.stream()
            .flatMap(article -> normalizedProductCodes.stream()
                .map(productCode -> buildRelation(
                    article,
                    themeCode,
                    themeName,
                    productCode,
                    keywords,
                    now
                )))
            .toList();
    }

    /**
     * 构建单条资讯显式关联。
     *
     * @param article 命中的资讯
     * @param themeCode 投资主题编码
     * @param themeName 投资主题名称
     * @param productCode 关联产品代码
     * @param keywords 主题关键词集合
     * @param now 当前北京时间
     * @return 资讯主题产品关联
     * @author dz
     * @date 2026-06-21
     */
    private NewsArticleRelation buildRelation(
        NewsArticle article,
        String themeCode,
        String themeName,
        String productCode,
        List<String> keywords,
        LocalDateTime now
    ) {
        List<String> matchedKeywords = matchedKeywords(article, keywords);
        BigDecimal sourceQualityScore = sourceQualityScore(article.sourceCode());
        BigDecimal relationScore = relationScore(article, keywords, now);
        return NewsArticleRelation.builder()
            .bizId(ids.newBizId())
            .articleBizId(article.bizId())
            .themeCode(themeCode)
            .themeName(themeName)
            .productCode(productCode)
            .relationType("KEYWORD_MATCH")
            .matchedKeywords(matchedKeywords)
            .sourceQualityScore(sourceQualityScore)
            .relationScore(relationScore)
            .evidence(limit(article.title(), 1024))
            .createdAt(now)
            .build();
    }

    /**
     * 计算单篇资讯对主题热度的贡献分。
     *
     * @param article 命中的资讯
     * @param keywords 主题关键词集合
     * @param now 当前北京时间
     * @return 综合关联分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal relationScore(NewsArticle article, List<String> keywords, LocalDateTime now) {
        int matchedKeywordCount = matchedKeywords(article, keywords).size();
        BigDecimal keywordScore = BigDecimal.valueOf(Math.max(matchedKeywordCount, 1));
        BigDecimal sourceScore = sourceQualityScore(article.sourceCode());
        BigDecimal recencyScore = recencyScore(article.publishTime(), now);
        return keywordScore.multiply(sourceScore)
            .multiply(recencyScore)
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 识别资讯命中的关键词。
     *
     * @param article 待判断资讯
     * @param keywords 主题关键词集合
     * @return 命中的关键词集合
     * @author dz
     * @date 2026-06-21
     */
    private List<String> matchedKeywords(NewsArticle article, List<String> keywords) {
        String text = (article.title() + " " + article.summary() + " " + article.content())
            .toLowerCase();
        return keywords.stream()
            .filter(keyword -> text.contains(keyword.toLowerCase()))
            .distinct()
            .toList();
    }

    /**
     * 根据来源编码计算数据源质量分。
     *
     * @param sourceCode 数据来源编码
     * @return 数据源质量分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal sourceQualityScore(String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return NORMAL_SOURCE_SCORE;
        }
        if (sourceCode.contains("FALLBACK")) {
            return FALLBACK_SOURCE_SCORE;
        }
        if (TRUSTED_SOURCE_CODES.contains(sourceCode)) {
            return TRUSTED_SOURCE_SCORE;
        }
        return NORMAL_SOURCE_SCORE;
    }

    /**
     * 根据发布时间计算时效分。
     *
     * @param publishTime 资讯发布时间
     * @param now 当前北京时间
     * @return 时效分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal recencyScore(LocalDateTime publishTime, LocalDateTime now) {
        if (publishTime == null) {
            return BigDecimal.valueOf(0.6);
        }
        long ageHours = Math.max(0, ChronoUnit.HOURS.between(publishTime, now));
        if (ageHours <= 6) {
            return BigDecimal.valueOf(1);
        }
        if (ageHours <= 24) {
            return BigDecimal.valueOf(0.85);
        }
        if (ageHours <= 72) {
            return BigDecimal.valueOf(0.7);
        }
        return BigDecimal.valueOf(0.5);
    }

    /**
     * 计算热度快照整体数据质量分。
     *
     * @param articleCount 命中文章数
     * @param uniqueSourceCount 去重来源数
     * @param averageSourceQuality 平均来源质量分
     * @return 数据质量分
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal calculateDataQualityScore(
        int articleCount,
        long uniqueSourceCount,
        BigDecimal averageSourceQuality
    ) {
        BigDecimal sampleScore = BigDecimal.valueOf(Math.min(articleCount, 20))
            .divide(BigDecimal.valueOf(20), 4, RoundingMode.HALF_UP);
        BigDecimal diversityScore = BigDecimal.valueOf(Math.min(uniqueSourceCount, 5))
            .divide(BigDecimal.valueOf(5), 4, RoundingMode.HALF_UP);
        return sampleScore.multiply(BigDecimal.valueOf(0.35))
            .add(diversityScore.multiply(BigDecimal.valueOf(0.25)))
            .add(averageSourceQuality.min(MAX_SOURCE_QUALITY_SCORE).multiply(BigDecimal.valueOf(0.40)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 将质量分转换为可读等级。
     *
     * @param dataQualityScore 数据质量分
     * @return HIGH/MEDIUM/LOW
     * @author dz
     * @date 2026-06-21
     */
    private String resolveQualityLevel(BigDecimal dataQualityScore) {
        if (dataQualityScore.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
            return "HIGH";
        }
        if (dataQualityScore.compareTo(BigDecimal.valueOf(0.45)) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    /**
     * 序列化热度计算依据。
     *
     * @param themeName 投资主题名称
     * @param keywords 主题关键词集合
     * @param metrics 热度质量指标
     * @param matchedArticles 命中资讯集合
     * @return 可解释指标 JSON
     * @author dz
     * @date 2026-06-21
     */
    private String writeMetrics(
        String themeName,
        List<String> keywords,
        NewsHeatMetrics metrics,
        List<NewsArticle> matchedArticles
    ) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("themeName", themeName);
            payload.put("keywords", keywords);
            payload.put("articleCount", metrics.articleCount());
            payload.put("uniqueSourceCount", metrics.uniqueSourceCount());
            payload.put("averageSourceQuality", metrics.averageSourceQuality());
            payload.put("dataQualityScore", metrics.dataQualityScore());
            payload.put("qualityLevel", metrics.qualityLevel());
            payload.put("heatScore", metrics.heatScore());
            payload.put("sampleArticles", sampleArticles(matchedArticles));
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("资讯热度指标序列化失败", exception);
        }
    }

    /**
     * 构建快照中的新闻样本摘要。
     *
     * @param matchedArticles 命中的资讯集合
     * @return 最多十条新闻样本摘要
     * @author dz
     * @date 2026-06-21
     */
    private List<Map<String, Object>> sampleArticles(List<NewsArticle> matchedArticles) {
        return matchedArticles.stream()
            .limit(10)
            .map(article -> {
                Map<String, Object> sample = new LinkedHashMap<>();
                sample.put("articleBizId", article.bizId());
                sample.put("title", article.title());
                sample.put("sourceCode", article.sourceCode());
                sample.put("publishTime", article.publishTime());
                return sample;
            })
            .toList();
    }

    /**
     * 限制文本长度。
     *
     * @param value 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     * @author dz
     * @date 2026-06-21
     */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 资讯热度及质量指标。 */
    @lombok.Builder
    private record NewsHeatMetrics(
        int articleCount,
        long uniqueSourceCount,
        BigDecimal averageSourceQuality,
        BigDecimal dataQualityScore,
        BigDecimal heatScore,
        String qualityLevel
    ) {
    }
}
