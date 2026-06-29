package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.ai.AiPromptEvaluation;
import com.example.dzcom.domain.model.ai.AiPromptOutputSchema;
import com.example.dzcom.domain.model.ai.AiPromptTemplate;
import com.example.dzcom.domain.model.ai.AiPromptVariable;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationStore;
import com.example.dzcom.domain.repository.ai.AiPromptStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 自动 Prompt 治理任务，用真实报告和反馈沉淀可复盘的 Prompt 资产。 */
@Service
@RequiredArgsConstructor
public class AutoPromptGovernanceTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AUTO_PROMPT_GOVERNANCE";
    private static final String DEFAULT_PROMPT_CODE = "investment-plan-from-report";
    private static final String DEFAULT_PROMPT_VERSION = "auto-v1";
    private static final String DEFAULT_SCENARIO = "INVESTMENT_PLAN";

    private final AiPromptStore prompts;
    private final AiPromptEvaluationStore evaluations;
    private final InvestmentAnalysisReportStore reports;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 判断当前处理器是否支持自动 Prompt 治理任务。
     *
     * @param taskType 任务类型
     * @return 类型为 AUTO_PROMPT_GOVERNANCE 时返回 true
     * @author dz
     * @date 2026-06-24
     */
    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    /**
     * 自动维护投资方案 Prompt 基线，并对最近真实报告写入 Prompt 评估记录。
     *
     * <p>任务不会凭空生成投资结论。没有报告时只创建或刷新 Prompt 基线配置，
     * 并返回等待真实报告的摘要；有报告时根据数据质量、可信等级和报告状态写入
     * 可前端查询的评估记录，为后续版本优化提供复盘入口。</p>
     *
     * @param event 任务触发事件
     * @return 执行摘要
     * @author dz
     * @date 2026-06-24
     */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        LocalDateTime now = clock.now();
        String promptCode = TaskParameterParser.string(event.parameters(), "promptCode", DEFAULT_PROMPT_CODE);
        String promptVersion = TaskParameterParser.string(event.parameters(), "promptVersion", DEFAULT_PROMPT_VERSION);
        String scenario = TaskParameterParser.string(event.parameters(), "scenario", DEFAULT_SCENARIO);
        String reportBizId = TaskParameterParser.string(event.parameters(), "reportBizId", "");
        AiPromptTemplate template = ensurePromptTemplate(promptCode, promptVersion, scenario, now);

        int sampleSize = TaskParameterParser.positiveInt(event.parameters(), "reportSampleSize", 20);
        List<InvestmentAnalysisReport> reportSamples = resolveReportSamples(reportBizId, sampleSize);
        if (reportSamples.isEmpty()) {
            return Jsons.toJson(Map.of(
                "status", "INITIALIZED",
                "promptBizId", template.bizId(),
                "promptCode", template.promptCode(),
                "promptVersion", template.promptVersion(),
                "scenario", template.scenario(),
                "readyForModel", false,
                "summary", "已初始化 Prompt 基线，暂无真实报告可评估，等待自动报告任务产出"
            ));
        }
        List<PromptRunArtifact> artifacts = reportSamples.stream()
            .map(report -> saveReportEvaluation(template, report, event.taskCode(), now))
            .toList();
        PromptRunArtifact primary = artifacts.get(0);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("status", "SUCCEEDED");
        summary.put("promptBizId", template.bizId());
        summary.put("promptCode", template.promptCode());
        summary.put("promptVersion", template.promptVersion());
        summary.put("scenario", template.scenario());
        summary.put("reportBizId", primary.reportBizId());
        summary.put("evaluationBizId", primary.evaluationBizId());
        summary.put("readyForModel", primary.readyForModel());
        summary.put("missingVariables", primary.missingVariables());
        summary.put("renderedPromptPreview", primary.renderedPromptPreview());
        summary.put("evaluatedReportCount", artifacts.size());
        summary.put("summary", "已为报告 " + primary.reportBizId() + " 渲染 Prompt，并基于 "
            + artifacts.size() + " 份真实报告写入 Prompt 治理评估");
        return Jsons.toJson(summary);
    }

    /** 解析本轮优先报告；没有指定时回退到最近报告窗口。 */
    private List<InvestmentAnalysisReport> resolveReportSamples(String reportBizId, int sampleSize) {
        if (reportBizId != null && !reportBizId.isBlank()) {
            List<InvestmentAnalysisReport> samples = new ArrayList<>();
            reports.findByBizId(reportBizId).ifPresent(samples::add);
            reports.latest(sampleSize).items().stream()
                .filter(report -> !report.bizId().equals(reportBizId))
                .limit(Math.max(0, sampleSize - samples.size()))
                .forEach(samples::add);
            return samples;
        }
        PageResult<InvestmentAnalysisReport> latestReports = reports.latest(sampleSize);
        return latestReports.items();
    }

    /** 确保默认投资方案 Prompt 模板、变量和输出 Schema 可被前端查看和配置。 */
    private AiPromptTemplate ensurePromptTemplate(
        String promptCode,
        String promptVersion,
        String scenario,
        LocalDateTime now
    ) {
        AiPromptTemplate existing = prompts.findTemplateByCodeAndVersion(promptCode, promptVersion).orElse(null);
        String promptBizId = existing == null ? ids.newBizId() : existing.bizId();
        AiPromptTemplate template = AiPromptTemplate.builder()
            .bizId(promptBizId)
            .promptCode(promptCode)
            .promptVersion(promptVersion)
            .scenario(scenario)
            .templateName("自动投资报告转方案 Prompt")
            .templateContent(templateContent())
            .status("ACTIVE")
            .description("由自动 Prompt 治理任务维护的基线模板；前端可复制新版本调整。")
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? "AUTO_PROMPT_GOVERNANCE" : existing.createdBy())
            .updatedBy("AUTO_PROMPT_GOVERNANCE")
            .build();
        AiPromptTemplate saved = prompts.saveTemplate(template);
        prompts.replaceVariables(saved.bizId(), variables(saved.bizId(), now));
        prompts.replaceOutputSchemas(saved.bizId(), outputSchemas(saved.bizId(), now));
        return saved;
    }

    /** 保存单份报告对应的 Prompt 评估。 */
    private PromptRunArtifact saveReportEvaluation(
        AiPromptTemplate template,
        InvestmentAnalysisReport report,
        String taskCode,
        LocalDateTime now
    ) {
        BigDecimal score = reportScore(report);
        PromptRenderResult render = renderPrompt(template, report);
        AiPromptEvaluation evaluation = evaluations.save(AiPromptEvaluation.builder()
            .bizId(ids.newBizId())
            .promptBizId(template.bizId())
            .promptCode(template.promptCode())
            .promptVersion(template.promptVersion())
            .scenario("REPORT_PROMPT_GOVERNANCE")
            .backtestBizId(null)
            .feedbackBizId(null)
            .score(score)
            .scoreDetail(Jsons.toJson(mapOfNullable(
                "reportBizId", report.bizId(),
                "reportStatus", blankToDefault(report.status(), "UNKNOWN"),
                "confidenceLevel", blankToDefault(report.confidenceLevel(), "UNKNOWN"),
                "dataQualityScore", report.dataQualityScore() == null ? BigDecimal.ZERO : report.dataQualityScore(),
                "themeCode", report.themeCode() == null ? "" : report.themeCode(),
                "readyForModel", render.readyForModel(),
                "missingVariables", render.missingVariables(),
                "renderedPromptPreview", previewText(render.renderedPrompt(), 600)
            )))
            .reviewStatus(reviewStatus(score))
            .evaluatorType("JOB")
            .evaluatorBizId(taskCode)
            .evaluatedAt(now)
            .createdAt(now)
            .build());
        return new PromptRunArtifact(
            report.bizId(),
            evaluation.bizId(),
            render.readyForModel(),
            render.missingVariables(),
            previewText(render.renderedPrompt(), 1000)
        );
    }

    /** 根据模板变量和报告 JSON 渲染本轮可运行 Prompt。 */
    private PromptRenderResult renderPrompt(AiPromptTemplate template, InvestmentAnalysisReport report) {
        Map<String, String> variables = Map.of(
            "investmentReport", reportPayload(report),
            "dataQualityGate", blankToDefault(report.dataQualityGate(), "{}"),
            "riskBoundary", riskBoundary(report),
            "outputSchema", prompts.findOutputSchemas(template.bizId()).stream()
                .findFirst()
                .map(AiPromptOutputSchema::schemaJson)
                .orElse("{}")
        );
        List<String> missing = prompts.findVariables(template.bizId()).stream()
            .filter(AiPromptVariable::required)
            .map(AiPromptVariable::variableName)
            .filter(name -> !variables.containsKey(name) || variables.get(name) == null || variables.get(name).isBlank())
            .toList();
        String rendered = template.templateContent();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return new PromptRenderResult(rendered, missing.isEmpty() && "ACTIVE".equals(template.status()), missing);
    }

    /** 构造报告变量快照。 */
    private String reportPayload(InvestmentAnalysisReport report) {
        return Jsons.toJson(mapOfNullable(
            "reportBizId", report.bizId(),
            "themeCode", report.themeCode(),
            "themeName", report.themeName(),
            "status", report.status(),
            "confidenceLevel", report.confidenceLevel(),
            "dataQualityScore", report.dataQualityScore(),
            "investmentSummary", Jsons.readObjectMapOrEmpty(report.investmentSummary()),
            "trend", Jsons.readObjectMapOrEmpty(report.trend()),
            "investmentPlan", Jsons.readObjectMapOrEmpty(report.investmentPlan()),
            "simulatedReturn", Jsons.readObjectMapOrEmpty(report.simulatedReturn())
        ));
    }

    /** 构造自动 Mock 边界变量。 */
    private String riskBoundary(InvestmentAnalysisReport report) {
        return Jsons.toJson(mapOfNullable(
            "mockOnly", true,
            "realTradeAllowed", false,
            "minDataQualityScore", "0.45",
            "reportBizId", report.bizId(),
            "confidenceLevel", report.confidenceLevel(),
            "dataQualityGate", Jsons.readObjectMapOrEmpty(report.dataQualityGate())
        ));
    }

    /** 根据报告状态、可信等级和数据质量形成 0-1 的治理评分。 */
    private BigDecimal reportScore(InvestmentAnalysisReport report) {
        BigDecimal quality = report.dataQualityScore() == null ? BigDecimal.ZERO : report.dataQualityScore();
        BigDecimal confidence = switch (report.confidenceLevel() == null ? "" : report.confidenceLevel()) {
            case "HIGH_CONFIDENCE" -> new BigDecimal("0.95");
            case "MEDIUM_CONFIDENCE" -> new BigDecimal("0.70");
            case "LOW_CONFIDENCE" -> new BigDecimal("0.40");
            default -> new BigDecimal("0.15");
        };
        BigDecimal status = "SUCCEEDED".equals(report.status()) ? BigDecimal.ONE : new BigDecimal("0.20");
        return quality.add(confidence).add(status).divide(new BigDecimal("3"), 4, java.math.RoundingMode.HALF_UP);
    }

    /** 根据评分给出复核状态。 */
    private String reviewStatus(BigDecimal score) {
        return score.compareTo(new BigDecimal("0.70")) >= 0 ? "PENDING" : "REJECTED";
    }

    /** 空文本转默认值，避免评估明细 JSON 写入空对象失败。 */
    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /** 截断长文本供步骤和评估摘要展示。 */
    private String previewText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    /** 构造允许值为空的有序摘要 Map。 */
    private Map<String, Object> mapOfNullable(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put((String) values[index], values[index + 1]);
        }
        return result;
    }

    /** 基线 Prompt 内容。 */
    private String templateContent() {
        return """
            你是投资辅助平台的方案生成模型。请只基于 ${investmentReport}、${dataQualityGate}、${riskBoundary} 输出结构化方案。
            若数据质量不足、产品风险画像缺失、用户风险不匹配或行情不新鲜，必须返回 dataGap 与 riskNotice，不得输出积极配置建议。
            输出必须符合 ${outputSchema}。
            """;
    }

    /** Prompt 变量定义。 */
    private List<AiPromptVariable> variables(String promptBizId, LocalDateTime now) {
        return List.of(
            variable(promptBizId, "investmentReport", "report", "投资报告完整 JSON", now),
            variable(promptBizId, "dataQualityGate", "report.dataQualityGate", "数据质量门禁和降级原因", now),
            variable(promptBizId, "riskBoundary", "user.riskProfile", "用户风险等级、产品适配和 Mock 交易边界", now),
            variable(promptBizId, "outputSchema", "prompt.outputSchema", "投资方案输出 JSON Schema", now)
        );
    }

    /** 构造单个 Prompt 变量。 */
    private AiPromptVariable variable(
        String promptBizId,
        String variableName,
        String sourcePath,
        String description,
        LocalDateTime now
    ) {
        return AiPromptVariable.builder()
            .bizId(ids.newBizId())
            .promptBizId(promptBizId)
            .variableName(variableName)
            .sourcePath(sourcePath)
            .required(true)
            .description(description)
            .createdAt(now)
            .build();
    }

    /** Prompt 输出 Schema 定义。 */
    private List<AiPromptOutputSchema> outputSchemas(String promptBizId, LocalDateTime now) {
        return List.of(AiPromptOutputSchema.builder()
            .bizId(ids.newBizId())
            .promptBizId(promptBizId)
            .schemaVersion("v1")
            .schemaJson("""
                {
                  "type": "object",
                  "required": ["summary", "actions", "riskNotice", "dataGap"],
                  "properties": {
                    "summary": {"type": "string"},
                    "actions": {"type": "array"},
                    "riskNotice": {"type": "array"},
                    "dataGap": {"type": "array"}
                  }
                }
                """)
            .createdAt(now)
            .build());
    }

    /** 本轮 Prompt 渲染结果。 */
    private record PromptRenderResult(String renderedPrompt, boolean readyForModel, List<String> missingVariables) {
    }

    /** 本轮 Prompt 产物摘要。 */
    private record PromptRunArtifact(
        String reportBizId,
        String evaluationBizId,
        boolean readyForModel,
        List<String> missingVariables,
        String renderedPromptPreview
    ) {
    }
}
