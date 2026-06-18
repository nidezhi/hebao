package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.application.service.task.TaskParameterParser;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 基于已入库快照的本地规则投资分析提供方。 */
@Component
@RequiredArgsConstructor
public class LocalRuleInvestmentAnalysisProvider implements InvestmentAnalysisProvider {
    private final InvestmentThemeSnapshotStore snapshots;
    private final NewsArticleStore articles;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /**
     * 判断是否支持本地规则分析提供方。
     *
     * @param providerCode 分析请求指定的提供方编码
     * @return providerCode 为 LOCAL_RULE 时返回 true
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public boolean supports(String providerCode) {
        return "LOCAL_RULE".equals(providerCode);
    }

    /**
     * 基于已入库主题快照和近期资讯生成结构化投资分析报告。
     *
     * <p>该实现不调用外部大模型，主要用于提供稳定的默认分析能力和 Provider
     * 扩展示例。输出包括投资信息汇总、趋势、参考方案、模拟收益和图表数据。</p>
     *
     * @param requestId 单次分析请求追踪标识
     * @param command 市场范围、主题、回看窗口和模拟资金参数
     * @param modelConfig 本地规则模型的运行配置
     * @return 可直接落库并返回前端的结构化分析报告
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public InvestmentAnalysisReport analyze(
        String requestId,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        AnalysisContext context = buildContext(command);
        return InvestmentAnalysisReport.builder()
            .bizId(ids.newBizId())
            .requestId(requestId)
            .providerCode("LOCAL_RULE")
            .modelCode(modelConfig.modelCode())
            .marketScope(context.marketScope())
            .themeCode(context.themeCode())
            .themeName(context.themeName())
            .status("SUCCEEDED")
            .investmentSummary(writeJson(buildInvestmentSummary(context)))
            .trend(writeJson(buildTrend(context)))
            .investmentPlan(writeJson(buildInvestmentPlan(context)))
            .simulatedReturn(writeJson(buildSimulatedReturn(context)))
            .chartPayload(writeJson(buildChartPayload(context)))
            .promptSnapshot(writeJson(buildInputSnapshot(context)))
            .generatedAt(context.generatedAt())
            .createdAt(context.generatedAt())
            .build();
    }

    /**
     * 查询分析所需快照和资讯并计算公共指标。
     *
     * @param command 分析命令
     * @return 后续各输出模块共享的分析上下文
     * @author dz
     * @date 2026-06-18
     */
    private AnalysisContext buildContext(GenerateInvestmentAnalysisCommand command) {
        LocalDateTime generatedAt = clock.now();
        String marketScope = resolveMarketScope(command.marketScope());
        String themeCode = command.themeCode() == null ? "" : command.themeCode();
        int lookbackDays = resolveLookbackDays(command.lookbackDays());
        LocalDateTime analysisFrom = generatedAt.minusDays(lookbackDays);

        List<InvestmentThemeSnapshot> themeSnapshots = snapshots.search(
            new InvestmentThemeSnapshotSearchCriteria(
            null,
            null,
            command.themeCode(),
            marketScope,
                analysisFrom,
                generatedAt,
            1,
            100,
            "snapshotTime",
            false
            )
        ).items();
        List<String> newsKeywords = themeCode.isBlank() ? List.of() : List.of(themeCode);
        List<NewsArticle> recentNews = articles.findRecentByKeywords(
            newsKeywords,
            analysisFrom,
            20
        );
        InvestmentThemeSnapshot latestSnapshot = themeSnapshots.stream()
            .max(Comparator.comparing(InvestmentThemeSnapshot::snapshotTime))
            .orElse(null);
        List<BigDecimal> returnRates = themeSnapshots.stream()
            .map(InvestmentThemeSnapshot::returnRate)
            .filter(value -> value != null)
            .toList();
        BigDecimal averageReturn = average(returnRates);
        BigDecimal initialCapital = resolveInitialCapital(command.initialCapital());
        BigDecimal estimatedProfit = initialCapital.multiply(averageReturn)
            .setScale(4, RoundingMode.HALF_UP);

        return AnalysisContext.builder()
            .generatedAt(generatedAt)
            .marketScope(marketScope)
            .themeCode(themeCode)
            .themeName(latestSnapshot == null ? null : latestSnapshot.themeName())
            .lookbackDays(lookbackDays)
            .themeSnapshots(themeSnapshots)
            .recentNews(recentNews)
            .latestSnapshot(latestSnapshot)
            .averageReturn(averageReturn)
            .initialCapital(initialCapital)
            .estimatedProfit(estimatedProfit)
            .build();
    }

    /**
     * 构建投资信息汇总数据。
     *
     * @param context 已完成数据查询和公共指标计算的分析上下文
     * @return 包含样本、收益和近期资讯的有序结构
     * @author dz
     * @date 2026-06-18
     */
    private Map<String, Object> buildInvestmentSummary(AnalysisContext context) {
        List<Map<String, Object>> recentNews = context.recentNews().stream()
            .map(this::toNewsSummary)
            .toList();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("marketScope", context.marketScope());
        summary.put("themeCode", context.themeCode());
        summary.put("sampleCount", context.themeSnapshots().size());
        summary.put("newsCount", context.recentNews().size());
        summary.put("averageReturn", context.averageReturn());
        summary.put("latestSnapshotTime", context.latestSnapshot() == null
            ? ""
            : context.latestSnapshot().snapshotTime());
        summary.put("recentNews", recentNews);
        return summary;
    }

    /**
     * 将资讯转换为报告汇总中的轻量新闻结构。
     *
     * @param article 近期投资资讯
     * @return 标题、摘要、发布时间和来源组成的有序结构
     * @author dz
     * @date 2026-06-18
     */
    private Map<String, Object> toNewsSummary(NewsArticle article) {
        Map<String, Object> news = new LinkedHashMap<>();
        news.put("title", article.title());
        news.put("summary", article.summary() == null ? "" : article.summary());
        news.put("publishTime", article.publishTime());
        news.put("sourceCode", article.sourceCode());
        return news;
    }

    /** 构建趋势输出。 */
    private Map<String, Object> buildTrend(AnalysisContext context) {
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("direction", context.averageReturn().signum() >= 0 ? "UP" : "DOWN");
        trend.put("averageReturn", context.averageReturn());
        trend.put("newsHeat", context.recentNews().size());
        trend.put("lookbackDays", context.lookbackDays());
        return trend;
    }

    /** 构建参考投资方案和风险提示。 */
    private Map<String, Object> buildInvestmentPlan(AnalysisContext context) {
        String suggestedAction = context.averageReturn().signum() >= 0
            ? "关注并分批配置"
            : "降低仓位并等待确认";
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("planType", "REFERENCE_ALLOCATION");
        plan.put("riskNotice", "AI 分析仅为投资辅助信息，不构成收益承诺或自动交易指令。");
        plan.put("suggestedAction", suggestedAction);
        return plan;
    }

    /** 构建模拟收益结果。 */
    private Map<String, Object> buildSimulatedReturn(AnalysisContext context) {
        Map<String, Object> simulatedReturn = new LinkedHashMap<>();
        simulatedReturn.put("initialCapital", context.initialCapital());
        simulatedReturn.put("estimatedProfit", context.estimatedProfit());
        simulatedReturn.put(
            "estimatedFinalCapital",
            context.initialCapital().add(context.estimatedProfit())
        );
        simulatedReturn.put("returnRate", context.averageReturn());
        return simulatedReturn;
    }

    /** 构建前端收益、动量、热度和新闻事件图表数据。 */
    private Map<String, Object> buildChartPayload(AnalysisContext context) {
        List<Map<String, Object>> series = context.themeSnapshots().stream()
            .sorted(Comparator.comparing(InvestmentThemeSnapshot::snapshotTime))
            .map(this::toSnapshotChartPoint)
            .toList();
        List<Map<String, Object>> news = context.recentNews().stream()
            .map(this::toNewsChartPoint)
            .toList();
        Map<String, Object> chartPayload = new LinkedHashMap<>();
        chartPayload.put("series", series);
        chartPayload.put("news", news);
        return chartPayload;
    }

    /** 将主题快照转换为图表数据点。 */
    private Map<String, Object> toSnapshotChartPoint(InvestmentThemeSnapshot snapshot) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("time", snapshot.snapshotTime());
        point.put("snapshotType", snapshot.snapshotType());
        point.put("returnRate", defaultZero(snapshot.returnRate()));
        point.put("momentumScore", defaultZero(snapshot.momentumScore()));
        point.put("heatScore", defaultZero(snapshot.heatScore()));
        return point;
    }

    /** 将资讯转换为图表新闻事件点。 */
    private Map<String, Object> toNewsChartPoint(NewsArticle article) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("time", article.publishTime());
        point.put("title", article.title());
        point.put("sourceCode", article.sourceCode());
        return point;
    }

    /** 构建脱敏后的分析输入快照。 */
    private Map<String, Object> buildInputSnapshot(AnalysisContext context) {
        Map<String, Object> inputSnapshot = new LinkedHashMap<>();
        inputSnapshot.put("providerCode", "LOCAL_RULE");
        inputSnapshot.put("marketScope", context.marketScope());
        inputSnapshot.put("themeCode", context.themeCode());
        inputSnapshot.put("lookbackDays", context.lookbackDays());
        return inputSnapshot;
    }

    /** 解析市场范围，空值默认中国大陆。 */
    private String resolveMarketScope(String marketScope) {
        return marketScope == null || marketScope.isBlank()
            ? TaskParameterParser.CN_MAINLAND
            : marketScope;
    }

    /** 解析回看天数，空值或非正数默认 30 天。 */
    private int resolveLookbackDays(Integer lookbackDays) {
        return lookbackDays == null || lookbackDays < 1 ? 30 : lookbackDays;
    }

    /** 解析模拟收益初始资金，空值默认十万元。 */
    private BigDecimal resolveInitialCapital(BigDecimal initialCapital) {
        return initialCapital == null ? BigDecimal.valueOf(100000) : initialCapital;
    }

    /** 将可空指标转换为图表可直接使用的零值。 */
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 计算收益率均值，空集合返回零。
     *
     * @param values 非空收益率集合
     * @return 八位小数精度的平均收益率
     * @author dz
     * @date 2026-06-18
     */
    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 8, RoundingMode.HALF_UP);
    }

    /**
     * 将结构化分析片段序列化为 JSON。
     *
     * @param value 待序列化的有序分析结构
     * @return 可落库并返回前端的 JSON 字符串
     * @throws IllegalStateException 当 Jackson 序列化失败时抛出
     * @author dz
     * @date 2026-06-18
     */
    private String writeJson(Map<String, ?> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("投资分析结果序列化失败", exception);
        }
    }

    /** 本地规则分析各输出模块共享的只读上下文。 */
    @Builder
    private record AnalysisContext(
        LocalDateTime generatedAt,
        String marketScope,
        String themeCode,
        String themeName,
        int lookbackDays,
        List<InvestmentThemeSnapshot> themeSnapshots,
        List<NewsArticle> recentNews,
        InvestmentThemeSnapshot latestSnapshot,
        BigDecimal averageReturn,
        BigDecimal initialCapital,
        BigDecimal estimatedProfit
    ) {
    }
}
