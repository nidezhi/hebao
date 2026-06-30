package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.application.service.task.TaskParameterParser;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
    private static final BigDecimal MIN_REPORT_QUALITY_SCORE = BigDecimal.valueOf(0.45);
    private static final BigDecimal MAX_FALLBACK_NEWS_RATIO = BigDecimal.valueOf(0.20);
    private static final BigDecimal CORE_GATE_WEIGHT = new BigDecimal("0.75");
    private static final BigDecimal THEME_SIGNAL_WEIGHT = new BigDecimal("0.25");

    private final InvestmentThemeSnapshotStore snapshots;
    private final NewsArticleStore articles;
    private final DataSourceStore dataSources;
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
            .confidenceLevel(context.qualityGate().confidenceLevel())
            .dataQualityScore(context.dataQualityScore())
            .dataQualityGate(writeJson(buildDataQualityGate(context)))
            .investmentSummary(writeJson(buildInvestmentSummary(context)))
            .trend(writeJson(buildTrend(context)))
            .investmentPlan(writeJson(buildInvestmentPlan(context)))
            .simulatedReturn(writeJson(buildSimulatedReturn(context)))
            .chartPayload(writeJson(buildChartPayload(context)))
            .promptSnapshot(writeJson(buildInputSnapshot(context)))
            .chatSnapshot(null)
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
        String themeCode = trimToNull(command.themeCode());
        int lookbackDays = resolveLookbackDays(command.lookbackDays());
        LocalDateTime analysisFrom = generatedAt.minusDays(lookbackDays);

        List<InvestmentThemeSnapshot> themeSnapshots = snapshots.search(
            new InvestmentThemeSnapshotSearchCriteria(
            null,
            null,
            themeCode,
            marketScope,
                analysisFrom,
                generatedAt,
            1,
            100,
            "snapshotTime",
            false
            )
        ).items();
        List<String> newsKeywords = themeCode == null ? List.of() : List.of(themeCode);
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
        BigDecimal averageMomentum = average(themeSnapshots.stream()
            .map(InvestmentThemeSnapshot::momentumScore)
            .filter(value -> value != null)
            .toList());
        BigDecimal averageHeat = average(themeSnapshots.stream()
            .map(InvestmentThemeSnapshot::heatScore)
            .filter(value -> value != null)
            .toList());
        CoreDataGate coreDataGate = latestCoreDataGate();
        BigDecimal themeSignalScore = average(snapshotQualityScores(themeSnapshots));
        BigDecimal dataQualityScore = mergedDataQualityScore(coreDataGate, themeSignalScore);
        DataQualityGate qualityGate = buildQualityGate(
            themeSnapshots,
            recentNews,
            dataQualityScore,
            coreDataGate
        );
        BigDecimal initialCapital = resolveInitialCapital(command.initialCapital());
        BigDecimal allocationRate = qualityGate.passed()
            ? allocationRate(averageReturn, averageMomentum, dataQualityScore)
            : BigDecimal.ZERO;
        BigDecimal simulatedPrincipal = initialCapital.multiply(allocationRate)
            .setScale(4, RoundingMode.HALF_UP);
        BigDecimal estimatedProfit = simulatedPrincipal.multiply(averageReturn)
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
            .averageMomentum(averageMomentum)
            .averageHeat(averageHeat)
            .themeSignalScore(themeSignalScore)
            .coreDataGate(coreDataGate)
            .dataQualityScore(dataQualityScore)
            .dataQualityLevel(resolveQualityLevel(dataQualityScore))
            .qualityGate(qualityGate)
            .initialCapital(initialCapital)
            .allocationRate(allocationRate)
            .simulatedPrincipal(simulatedPrincipal)
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
        summary.put("averageMomentum", context.averageMomentum());
        summary.put("averageHeat", context.averageHeat());
        summary.put("dataQualityScore", context.dataQualityScore());
        summary.put("dataQualityLevel", context.dataQualityLevel());
        summary.put("confidenceLevel", context.qualityGate().confidenceLevel());
        summary.put("dataQualityPassed", context.qualityGate().passed());
        summary.put("dataGapReasons", context.qualityGate().reasons());
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
        trend.put("direction", resolveTrendDirection(context));
        trend.put("averageReturn", context.averageReturn());
        trend.put("averageMomentum", context.averageMomentum());
        trend.put("newsHeat", context.recentNews().size());
        trend.put("weightedHeatScore", context.averageHeat());
        trend.put("dataQualityScore", context.dataQualityScore());
        trend.put("confidenceLevel", context.qualityGate().confidenceLevel());
        trend.put("dataQualityPassed", context.qualityGate().passed());
        trend.put("lookbackDays", context.lookbackDays());
        return trend;
    }

    /**
     * 构建参考投资方案和风险提示。
     *
     * @param context 已完成数据查询和公共指标计算的分析上下文
     * @return 投资方案结构
     * @author dz
     * @date 2026-06-21
     */
    private Map<String, Object> buildInvestmentPlan(AnalysisContext context) {
        Map<String, Object> plan = new LinkedHashMap<>();
        if (!context.qualityGate().passed()) {
            plan.put("planType", "DATA_GAP_REPORT");
            plan.put("suggestedAction", context.qualityGate().displayMessage());
            plan.put("referenceAllocationRate", BigDecimal.ZERO);
            plan.put("referenceAllocationAmount", BigDecimal.ZERO);
            plan.put("dataQualityLevel", context.dataQualityLevel());
            plan.put("confidenceLevel", context.qualityGate().confidenceLevel());
            plan.put("dataGapReasons", context.qualityGate().reasons());
            plan.put("rebalanceRule", "数据质量未达标，禁止生成调仓或配置建议。");
            plan.put("riskNotice", "当前报告仅用于暴露数据缺口，不构成投资建议。");
            return plan;
        }
        plan.put("planType", "REFERENCE_ALLOCATION");
        plan.put("suggestedAction", suggestedAction(context));
        plan.put("referenceAllocationRate", context.allocationRate());
        plan.put("referenceAllocationAmount", context.simulatedPrincipal().setScale(2, RoundingMode.HALF_UP));
        plan.put("dataQualityLevel", context.dataQualityLevel());
        plan.put("rebalanceRule", "仅当收益、动量和资讯热度连续两个统计窗口同向时提高仓位。");
        plan.put("riskNotice", "AI 分析仅为投资辅助信息，不构成收益承诺或自动交易指令。");
        return plan;
    }

    /**
     * 构建模拟收益结果。
     *
     * @param context 已完成数据查询和公共指标计算的分析上下文
     * @return 模拟收益结构
     * @author dz
     * @date 2026-06-21
     */
    private Map<String, Object> buildSimulatedReturn(AnalysisContext context) {
        Map<String, Object> simulatedReturn = new LinkedHashMap<>();
        simulatedReturn.put("initialCapital", context.initialCapital());
        simulatedReturn.put("allocationRate", context.allocationRate());
        simulatedReturn.put("simulatedPrincipal", context.simulatedPrincipal());
        simulatedReturn.put("estimatedProfit", context.estimatedProfit());
        simulatedReturn.put(
            "estimatedFinalCapital",
            context.initialCapital().add(context.estimatedProfit())
        );
        simulatedReturn.put("returnRate", context.averageReturn());
        simulatedReturn.put("stressLoss", stressLoss(context));
        simulatedReturn.put("optimisticProfit", optimisticProfit(context));
        simulatedReturn.put("confidenceLevel", context.qualityGate().confidenceLevel());
        simulatedReturn.put("dataQualityPassed", context.qualityGate().passed());
        simulatedReturn.put("assumption", "按回看窗口平均收益率模拟，仅反映历史样本，不代表未来收益。");
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
        inputSnapshot.put("providerSelection", "ACTIVE_AI_MODEL_PROVIDER");
        inputSnapshot.put("marketScope", context.marketScope());
        inputSnapshot.put("themeCode", context.themeCode());
        inputSnapshot.put("lookbackDays", context.lookbackDays());
        inputSnapshot.put("dataQualityScore", context.dataQualityScore());
        inputSnapshot.put("dataQualityLevel", context.dataQualityLevel());
        inputSnapshot.put("confidenceLevel", context.qualityGate().confidenceLevel());
        inputSnapshot.put("dataQualityGate", buildDataQualityGate(context));
        return inputSnapshot;
    }

    /** 解析市场范围，空值默认中国大陆。 */
    private String resolveMarketScope(String marketScope) {
        return marketScope == null || marketScope.isBlank()
            ? TaskParameterParser.CN_MAINLAND
            : marketScope.trim();
    }

    /** 将空白主题标准化为空值，避免全市场报告落库为空字符串。 */
    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
     * 构建报告级数据质量门禁。
     *
     * <p>门禁结果直接落库并暴露给前端。未通过门禁时，报告只能展示数据缺口，
     * 不能生成配置比例、调仓方案或 Mock 交易入口。</p>
     *
     * @param themeSnapshots 投资主题快照样本
     * @param recentNews 近期新闻样本
     * @param dataQualityScore 快照解析得到的数据质量分
     * @return 报告质量门禁结果
     * @author dz
     * @date 2026-06-22
     */
    private DataQualityGate buildQualityGate(
        List<InvestmentThemeSnapshot> themeSnapshots,
        List<NewsArticle> recentNews,
        BigDecimal dataQualityScore,
        CoreDataGate coreDataGate
    ) {
        BigDecimal fallbackNewsRatio = fallbackNewsRatio(recentNews);
        List<String> reasons = buildQualityReasons(
            themeSnapshots,
            recentNews,
            dataQualityScore,
            fallbackNewsRatio,
            coreDataGate
        );
        boolean passed = reasons.isEmpty();
        String confidenceLevel = resolveConfidenceLevel(passed, dataQualityScore);
        return DataQualityGate.builder()
            .passed(passed)
            .confidenceLevel(confidenceLevel)
            .snapshotCount(themeSnapshots.size())
            .newsCount(recentNews.size())
            .coreDataQualityScore(coreDataGate.qualityScore())
            .coreDataReportAllowed(coreDataGate.reportAllowed())
            .themeSignalScore(average(snapshotQualityScores(themeSnapshots)))
            .fallbackNewsRatio(fallbackNewsRatio)
            .reasons(reasons)
            .displayMessage(displayMessage(passed, reasons))
            .allowedActions(allowedActions(passed))
            .build();
    }

    /**
     * 构建前端可展示的数据质量门禁 JSON。
     *
     * @param context 分析上下文
     * @return 数据质量门禁有序结构
     * @author dz
     * @date 2026-06-22
     */
    private Map<String, Object> buildDataQualityGate(AnalysisContext context) {
        Map<String, Object> gate = new LinkedHashMap<>();
        gate.put("passed", context.qualityGate().passed());
        gate.put("confidenceLevel", context.qualityGate().confidenceLevel());
        gate.put("dataQualityScore", context.dataQualityScore());
        gate.put("snapshotCount", context.qualityGate().snapshotCount());
        gate.put("newsCount", context.qualityGate().newsCount());
        gate.put("coreDataQualityScore", context.qualityGate().coreDataQualityScore());
        gate.put("coreDataReportAllowed", context.qualityGate().coreDataReportAllowed());
        gate.put("themeSignalScore", context.qualityGate().themeSignalScore());
        gate.put("fallbackNewsRatio", context.qualityGate().fallbackNewsRatio());
        gate.put("reasons", context.qualityGate().reasons());
        gate.put("displayMessage", context.qualityGate().displayMessage());
        gate.put("allowedActions", context.qualityGate().allowedActions());
        return gate;
    }

    /**
     * 汇总报告降级原因。
     *
     * @param themeSnapshots 投资主题快照样本
     * @param recentNews 近期新闻样本
     * @param dataQualityScore 数据质量分
     * @param fallbackNewsRatio 兜底资讯占比
     * @return 降级原因编码集合
     * @author dz
     * @date 2026-06-22
     */
    private List<String> buildQualityReasons(
        List<InvestmentThemeSnapshot> themeSnapshots,
        List<NewsArticle> recentNews,
        BigDecimal dataQualityScore,
        BigDecimal fallbackNewsRatio,
        CoreDataGate coreDataGate
    ) {
        Map<String, Boolean> checks = new LinkedHashMap<>();
        checks.put("CORE_DATA_GATE_NOT_PASSED", !coreDataGate.reportAllowed());
        checks.put("NO_RECENT_NEWS", recentNews.isEmpty() && !coreDataGate.reportAllowed());
        checks.put("LOW_DATA_QUALITY", dataQualityScore.compareTo(MIN_REPORT_QUALITY_SCORE) < 0);
        checks.put("FALLBACK_NEWS_RATIO_TOO_HIGH",
            fallbackNewsRatio.compareTo(MAX_FALLBACK_NEWS_RATIO) > 0);
        return checks.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * 计算兜底资讯占比。
     *
     * @param recentNews 近期新闻样本
     * @return 兜底资讯数量占比，0-1
     * @author dz
     * @date 2026-06-22
     */
    private BigDecimal fallbackNewsRatio(List<NewsArticle> recentNews) {
        if (recentNews.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long fallbackCount = recentNews.stream()
            .map(NewsArticle::sourceCode)
            .filter(sourceCode -> sourceCode != null && sourceCode.contains("FALLBACK"))
            .count();
        return BigDecimal.valueOf(fallbackCount)
            .divide(BigDecimal.valueOf(recentNews.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 根据门禁结果和质量分确定报告可信等级。
     *
     * @param passed 是否通过质量门禁
     * @param dataQualityScore 数据质量分
     * @return 报告可信等级
     * @author dz
     * @date 2026-06-22
     */
    private String resolveConfidenceLevel(boolean passed, BigDecimal dataQualityScore) {
        if (!passed && dataQualityScore.compareTo(BigDecimal.ZERO) == 0) {
            return "UNUSABLE";
        }
        if (!passed) {
            return "LOW_CONFIDENCE";
        }
        if (dataQualityScore.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
            return "HIGH_CONFIDENCE";
        }
        return "MEDIUM_CONFIDENCE";
    }

    /**
     * 构建前端直接展示的门禁提示文案。
     *
     * @param passed 是否通过质量门禁
     * @param reasons 降级原因编码
     * @return 中文展示文案
     * @author dz
     * @date 2026-06-22
     */
    private String displayMessage(boolean passed, List<String> reasons) {
        if (passed) {
            return "数据质量已达到生成参考投资方案的最低要求。";
        }
        return "数据质量不足，当前仅展示数据缺口，不生成投资配置建议。原因：" + String.join(",", reasons);
    }

    /**
     * 构建前端允许展示的动作集合。
     *
     * @param passed 是否通过质量门禁
     * @return 允许动作编码集合
     * @author dz
     * @date 2026-06-22
     */
    private List<String> allowedActions(boolean passed) {
        if (passed) {
            return List.of("VIEW_REPORT", "GENERATE_PROMPT", "MOCK_TRADE");
        }
        return List.of("VIEW_REPORT", "SHOW_DATA_GAP");
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
     * 从快照 metrics 中读取统计质量分。
     *
     * @param themeSnapshots 主题快照集合
     * @return 已解析的质量分集合
     * @author dz
     * @date 2026-06-21
     */
    private List<BigDecimal> snapshotQualityScores(List<InvestmentThemeSnapshot> themeSnapshots) {
        return themeSnapshots.stream()
            .map(InvestmentThemeSnapshot::metrics)
            .map(this::readQualityScore)
            .filter(value -> value != null)
            .toList();
    }

    /** 读取最新真实核心数据门禁。 */
    private CoreDataGate latestCoreDataGate() {
        return dataSources.findQualitySnapshots("REAL_DATA_GATE", "CORE_DATA_GATE", 1).stream()
            .findFirst()
            .map(snapshot -> CoreDataGate.builder()
                .qualityScore(defaultZero(snapshot.qualityScore()))
                .reportAllowed(readReportAllowed(snapshot))
                .snapshotTime(snapshot.snapshotTime())
                .sampleCount(snapshot.sampleCount())
                .build())
            .orElse(CoreDataGate.builder()
                .qualityScore(BigDecimal.ZERO)
                .reportAllowed(false)
                .sampleCount(0)
                .build());
    }

    /** 合并真实核心门禁和主题信号质量。 */
    private BigDecimal mergedDataQualityScore(CoreDataGate coreDataGate, BigDecimal themeSignalScore) {
        if (coreDataGate.reportAllowed()) {
            return coreDataGate.qualityScore().multiply(CORE_GATE_WEIGHT)
                .add(themeSignalScore.multiply(THEME_SIGNAL_WEIGHT))
                .setScale(4, RoundingMode.HALF_UP);
        }
        return themeSignalScore.setScale(4, RoundingMode.HALF_UP);
    }

    /** 从质量快照 detail 判断报告是否允许。 */
    private boolean readReportAllowed(DataQualitySnapshot snapshot) {
        if (snapshot.detail() == null || snapshot.detail().isBlank()) {
            return false;
        }
        try {
            return objectMapper.readTree(snapshot.detail()).path("reportAllowed").asBoolean(false);
        } catch (JsonProcessingException exception) {
            return false;
        }
    }

    /**
     * 从单条 metrics JSON 中读取质量分。
     *
     * @param metrics 快照指标 JSON
     * @return 质量分；无法读取时返回 null
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal readQualityScore(String metrics) {
        if (metrics == null || metrics.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(metrics);
            JsonNode score = root.hasNonNull("dataQualityScore")
                ? root.get("dataQualityScore")
                : root.get("qualityScore");
            if (score == null || !score.isNumber()) {
                return null;
            }
            return score.decimalValue();
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    /**
     * 根据收益、动量和数据质量计算参考配置比例。
     *
     * @param averageReturn 平均收益
     * @param averageMomentum 平均动量
     * @param dataQualityScore 数据质量分
     * @return 参考配置比例
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal allocationRate(
        BigDecimal averageReturn,
        BigDecimal averageMomentum,
        BigDecimal dataQualityScore
    ) {
        if (dataQualityScore.compareTo(BigDecimal.valueOf(0.45)) < 0) {
            return BigDecimal.valueOf(0.10);
        }
        if (averageReturn.signum() > 0 && averageMomentum.signum() > 0) {
            return BigDecimal.valueOf(0.30);
        }
        if (averageReturn.signum() >= 0) {
            return BigDecimal.valueOf(0.20);
        }
        return BigDecimal.valueOf(0.05);
    }

    /**
     * 判断趋势方向。
     *
     * @param context 分析上下文
     * @return UP/NEUTRAL/DOWN
     * @author dz
     * @date 2026-06-21
     */
    private String resolveTrendDirection(AnalysisContext context) {
        if (context.averageReturn().signum() > 0 && context.averageMomentum().signum() > 0) {
            return "UP";
        }
        if (context.averageReturn().signum() < 0 && context.averageMomentum().signum() <= 0) {
            return "DOWN";
        }
        return "NEUTRAL";
    }

    /**
     * 生成建议动作。
     *
     * @param context 分析上下文
     * @return 建议动作
     * @author dz
     * @date 2026-06-21
     */
    private String suggestedAction(AnalysisContext context) {
        if ("LOW".equals(context.dataQualityLevel())) {
            return "数据质量不足，维持观察仓位并补充数据源";
        }
        if ("UP".equals(resolveTrendDirection(context))) {
            return "关注并分批配置";
        }
        if ("NEUTRAL".equals(resolveTrendDirection(context))) {
            return "轻仓跟踪并等待趋势确认";
        }
        return "降低仓位并等待确认";
    }

    /**
     * 计算压力情景亏损。
     *
     * @param context 分析上下文
     * @return 压力情景收益
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal stressLoss(AnalysisContext context) {
        return context.simulatedPrincipal()
            .multiply(context.averageReturn().subtract(BigDecimal.valueOf(0.05)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 计算乐观情景收益。
     *
     * @param context 分析上下文
     * @return 乐观情景收益
     * @author dz
     * @date 2026-06-21
     */
    private BigDecimal optimisticProfit(AnalysisContext context) {
        return context.simulatedPrincipal()
            .multiply(context.averageReturn().add(BigDecimal.valueOf(0.03)))
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 将质量分转换为等级。
     *
     * @param qualityScore 质量分
     * @return HIGH/MEDIUM/LOW
     * @author dz
     * @date 2026-06-21
     */
    private String resolveQualityLevel(BigDecimal qualityScore) {
        if (qualityScore.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
            return "HIGH";
        }
        if (qualityScore.compareTo(BigDecimal.valueOf(0.45)) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
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
        BigDecimal averageMomentum,
        BigDecimal averageHeat,
        BigDecimal themeSignalScore,
        CoreDataGate coreDataGate,
        BigDecimal dataQualityScore,
        String dataQualityLevel,
        DataQualityGate qualityGate,
        BigDecimal initialCapital,
        BigDecimal allocationRate,
        BigDecimal simulatedPrincipal,
        BigDecimal estimatedProfit
    ) {
    }

    /** 报告级数据质量门禁结果。 */
    @Builder
    private record DataQualityGate(
        boolean passed,
        String confidenceLevel,
        int snapshotCount,
        int newsCount,
        BigDecimal coreDataQualityScore,
        boolean coreDataReportAllowed,
        BigDecimal themeSignalScore,
        BigDecimal fallbackNewsRatio,
        List<String> reasons,
        String displayMessage,
        List<String> allowedActions
    ) {
    }

    /** 真实核心数据门禁摘要。 */
    @Builder
    private record CoreDataGate(
        BigDecimal qualityScore,
        boolean reportAllowed,
        LocalDateTime snapshotTime,
        int sampleCount
    ) {
    }
}
