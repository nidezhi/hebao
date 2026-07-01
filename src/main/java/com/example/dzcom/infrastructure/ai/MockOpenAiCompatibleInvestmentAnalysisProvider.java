package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.dto.ai.AiModelCallAuditContext;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.AiModelCallAuditApplicationService;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * OpenAI 兼容协议投资分析 Provider。
 *
 * <p>当前闭环要求真实调用远程模型。Provider 会先用本地规则生成可信输入基线，
 * 再强制调用 OpenAI Chat Completions 兼容接口；如果配置仍处于 mock 模式或远程
 * 调用失败，会记录错误日志并阻断任务。</p>
 */
@Component
@Slf4j
public class MockOpenAiCompatibleInvestmentAnalysisProvider
    implements InvestmentAnalysisProvider {

    private static final String PROVIDER_CODE = "OPENAI_COMPATIBLE";

    private final LocalRuleInvestmentAnalysisProvider localRuleProvider;
    private final ObjectMapper objectMapper;
    private final AiModelCallAuditApplicationService callAudits;

    /**
     * 构造不启用持久化模型审计的 Provider。
     *
     * <p>该构造器保留给直接 new Provider 的单元测试；Spring 运行时使用带
     * {@link AiModelCallAuditApplicationService} 的构造器，以保证模型调用可审计。</p>
     *
     * @param localRuleProvider 本地规则分析 Provider
     * @param objectMapper JSON 组件
     */
    MockOpenAiCompatibleInvestmentAnalysisProvider(
        LocalRuleInvestmentAnalysisProvider localRuleProvider,
        ObjectMapper objectMapper
    ) {
        this(localRuleProvider, objectMapper, null);
    }

    /**
     * 构造生产运行的 OpenAI 兼容投资分析 Provider。
     *
     * @param localRuleProvider 本地规则分析 Provider，用于生成远端调用前的可信输入基线
     * @param objectMapper JSON 组件
     * @param callAudits 模型调用审计服务，用于持久化输入输出摘要和业务归因
     */
    @Autowired
    public MockOpenAiCompatibleInvestmentAnalysisProvider(
        LocalRuleInvestmentAnalysisProvider localRuleProvider,
        ObjectMapper objectMapper,
        AiModelCallAuditApplicationService callAudits
    ) {
        this.localRuleProvider = localRuleProvider;
        this.objectMapper = objectMapper;
        this.callAudits = callAudits;
    }

    /**
     * 判断是否支持 OpenAI 兼容协议模型。
     *
     * @param providerCode 模型表中配置的 Provider 编码
     * @return 编码为 OPENAI_COMPATIBLE 时返回 true
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public boolean supports(String providerCode) {
        return PROVIDER_CODE.equals(providerCode);
    }

    /**
     * 使用 OpenAI 兼容远程模型生成报告。
     *
     * @param requestId 单次分析请求追踪标识
     * @param command 市场范围、主题和模拟资金等业务参数
     * @param modelConfig 数据库配置与外部 API Key 组成的运行配置
     * @return 标记为 OPENAI_COMPATIBLE 模型生成的结构化报告
     * @throws BusinessException 当模型仍处于 mock 模式或远程调用失败时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public InvestmentAnalysisReport analyze(
        String requestId,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        InvestmentAnalysisReport localReport = localRuleProvider.analyze(
            requestId,
            command,
            modelConfig
        );
        if (!isLocalDataQualityGatePassed(localReport)) {
            log.warn(
                "投资分析模型跳过远端调用: requestId={}, modelCode={}, modelVersion={}, providerCode={}, remoteModel={}, marketScope={}, themeCode={}, lookbackDays={}, localQualityScore={}, localGate={}, reason=LOCAL_DATA_QUALITY_GATE_NOT_PASSED",
                requestId,
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                modelConfig.remoteModel(),
                command.marketScope(),
                command.themeCode(),
                command.lookbackDays(),
                localReport.dataQualityScore(),
                localReport.dataQualityGate()
            );
            return remoteSkippedDataGapReport(localReport, modelConfig);
        }
        log.info(
            "投资分析模型准备远端调用: requestId={}, modelCode={}, modelVersion={}, providerCode={}, remoteModel={}, marketScope={}, themeCode={}, lookbackDays={}, localQualityScore={}, localGate={}",
            requestId,
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.remoteModel(),
            command.marketScope(),
            command.themeCode(),
            command.lookbackDays(),
            localReport.dataQualityScore(),
            localReport.dataQualityGate()
        );
        return callRemoteModel(localReport, command, modelConfig);
    }

    /** 本地数据质量未通过时，不允许远端模型包装空数据。 */
    private boolean isLocalDataQualityGatePassed(InvestmentAnalysisReport localReport) {
        if (localReport.dataQualityGate() == null || localReport.dataQualityGate().isBlank()) {
            return false;
        }
        return readJson(localReport.dataQualityGate()).path("passed").asBoolean(false);
    }

    /** 返回带远端模型审计信息的数据缺口报告，不触发真实模型调用。 */
    private InvestmentAnalysisReport remoteSkippedDataGapReport(
        InvestmentAnalysisReport localReport,
        AiModelRuntimeConfig modelConfig
    ) {
        return InvestmentAnalysisReport.builder()
            .bizId(localReport.bizId())
            .requestId(localReport.requestId())
            .providerCode(PROVIDER_CODE)
            .modelCode(modelConfig.modelCode())
            .marketScope(localReport.marketScope())
            .themeCode(localReport.themeCode())
            .themeName(localReport.themeName())
            .status(localReport.status())
            .confidenceLevel(localReport.confidenceLevel())
            .dataQualityScore(localReport.dataQualityScore())
            .dataQualityGate(localReport.dataQualityGate())
            .investmentSummary(localReport.investmentSummary())
            .trend(localReport.trend())
            .investmentPlan(localReport.investmentPlan())
            .simulatedReturn(localReport.simulatedReturn())
            .chartPayload(localReport.chartPayload())
            .promptSnapshot(localReport.promptSnapshot())
            .chatSnapshot(null)
            .failureReason("SKIPPED_REMOTE_LOW_DATA_QUALITY")
            .generatedAt(localReport.generatedAt())
            .createdAt(localReport.createdAt())
            .build();
    }

    /**
     * 调用 OpenAI 兼容远端模型并转换为投资分析报告。
     *
     * @param localReport 本地规则生成的可信输入和降级基线
     * @param command 投资分析命令
     * @param modelConfig 模型运行时配置
     * @return 远端模型结构化输出转换后的报告
     * @author dz
     * @date 2026-06-24
     */
    private InvestmentAnalysisReport callRemoteModel(
        InvestmentAnalysisReport localReport,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        validateRemoteCallable(localReport.requestId(), modelConfig);
        long startedAt = System.nanoTime();
        String endpoint = resolveChatCompletionsUrl(modelConfig.baseUrl());
        String userPrompt = prompt(localReport, command);
        Map<String, Object> payload = requestPayload(userPrompt, modelConfig);
        String callId = UUID.randomUUID().toString();
        startAudit(callId, localReport, command, modelConfig, endpoint, userPrompt);
        log.info(
            "投资分析模型远端调用开始: requestId={}, modelCode={}, modelVersion={}, providerCode={}, remoteModel={}, endpoint={}, httpMethod=POST, secretRef={}, apiKeyConfigured={}, timeoutSeconds={}, maxTokens={}, temperature={}, userPromptLength={}, userPromptHash={}",
            localReport.requestId(),
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.remoteModel(),
            endpoint,
            modelConfig.secretRef(),
            modelConfig.apiKey() != null && !modelConfig.apiKey().isBlank(),
            modelConfig.timeoutSeconds(),
            modelConfig.maxTokens(),
            modelConfig.temperature(),
            userPrompt.length(),
            sha256(userPrompt)
        );
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .header("Authorization", "Bearer " + modelConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(payload)))
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            log.info(
                "投资分析模型远端响应: requestId={}, modelCode={}, modelVersion={}, providerCode={}, httpStatus={}, durationMs={}, responseLength={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                response.statusCode(),
                elapsedMs(startedAt),
                textLength(response.body())
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String failureMessage = remoteFailureMessage(response.statusCode(), response.body());
                failAudit(callId, response.statusCode(), elapsedMs(startedAt), sha256(response.body()),
                    response.body(), "HTTP_" + response.statusCode(), failureMessage);
                log.error(
                    "投资分析模型远端调用失败: requestId={}, modelCode={}, modelVersion={}, providerCode={}, endpoint={}, httpMethod=POST, httpStatus={}, durationMs={}, failureMessage={}, responseBody={}",
                    localReport.requestId(),
                    modelConfig.modelCode(),
                    modelConfig.modelVersion(),
                    modelConfig.providerCode(),
                    endpoint,
                    response.statusCode(),
                    elapsedMs(startedAt),
                    failureMessage,
                    limit(response.body(), 2000)
                );
                throw new BusinessException(HttpStatus.BAD_GATEWAY, failureMessage);
            }
            String content = extractContent(response.body());
            InvestmentAnalysisReport report = mergeRemoteOutput(
                localReport,
                modelConfig,
                content,
                buildChatSnapshot(payload, content, endpoint, response.statusCode(), elapsedMs(startedAt))
            );
            succeedAudit(callId, response.statusCode(), elapsedMs(startedAt), sha256(content), content,
                Map.of(
                    "reportBizId", report.bizId(),
                    "confidenceLevel", report.confidenceLevel(),
                    "dataQualityScore", report.dataQualityScore(),
                    "contentLength", textLength(content)
                ));
            log.info(
                "投资分析模型远端调用完成: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, contentLength={}, contentHash={}, qualityScore={}, confidenceLevel={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                textLength(content),
                sha256(content),
                report.dataQualityScore(),
                report.confidenceLevel()
            );
            return report;
        } catch (BusinessException exception) {
            failAudit(callId, null, elapsedMs(startedAt), "", "",
                "BUSINESS_EXCEPTION", exception.getMessage());
            log.error(
                "投资分析模型远端业务失败: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, reason={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                exception.getMessage()
            );
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            failAudit(callId, null, elapsedMs(startedAt), "", "",
                "INTERRUPTED", "OpenAI兼容模型调用被中断");
            log.error(
                "投资分析模型远端调用被中断: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt)
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用被中断");
        } catch (Exception exception) {
            failAudit(callId, null, elapsedMs(startedAt), "", "",
                exception.getClass().getSimpleName(), exception.getMessage());
            log.error(
                "投资分析模型远端调用异常: requestId={}, modelCode={}, modelVersion={}, providerCode={}, durationMs={}, exceptionType={}, reason={}",
                localReport.requestId(),
                modelConfig.modelCode(),
                modelConfig.modelVersion(),
                modelConfig.providerCode(),
                elapsedMs(startedAt),
                exception.getClass().getSimpleName(),
                exception.getMessage()
            );
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型调用失败: " + exception.getMessage());
        }
    }

    /**
     * 校验投资分析报告必须真实调用远程模型。
     *
     * @param requestId 单次分析请求追踪标识
     * @param modelConfig 模型运行时配置
     * @throws BusinessException 当配置不可用于远程调用时抛出
     * @author dz
     * @date 2026-06-27
     */
    private void validateRemoteCallable(String requestId, AiModelRuntimeConfig modelConfig) {
        if (modelConfig.mockEnabled()) {
            failRemoteCallable(requestId, modelConfig, "模型配置mockEnabled=true，禁止使用本地规则替代远程模型");
        }
        if (isBlank(modelConfig.baseUrl())) {
            failRemoteCallable(requestId, modelConfig, "模型baseUrl为空，不能调用远程模型");
        }
        if (isBlank(modelConfig.remoteModel())) {
            failRemoteCallable(requestId, modelConfig, "远端模型名称为空，不能调用远程模型");
        }
        if (isBlank(modelConfig.apiKey())) {
            failRemoteCallable(requestId, modelConfig, "模型API Key为空，不能调用远程模型");
        }
    }

    /** 输出投资分析远程调用前置配置错误并抛出业务异常。 */
    private void failRemoteCallable(String requestId, AiModelRuntimeConfig modelConfig, String reason) {
        log.error(
            "投资分析模型远程调用前置校验失败: requestId={}, modelCode={}, modelVersion={}, providerCode={}, secretRef={}, reason={}",
            requestId,
            modelConfig.modelCode(),
            modelConfig.modelVersion(),
            modelConfig.providerCode(),
            modelConfig.secretRef(),
            reason
        );
        throw new BusinessException(HttpStatus.BAD_REQUEST, "投资分析模型远程调用失败: " + reason);
    }

    /**
     * 构建 OpenAI Chat Completions 请求体。
     *
     * @param localReport 本地规则报告
     * @param command 投资分析命令
     * @param modelConfig 模型配置
     * @return 请求 JSON 结构
     * @author dz
     * @date 2026-06-24
     */
    private Map<String, Object> requestPayload(
        String userPrompt,
        AiModelRuntimeConfig modelConfig
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", modelConfig.remoteModel());
        payload.put("temperature", modelConfig.temperature());
        if (modelConfig.maxTokens() > 0) {
            payload.put("max_tokens", modelConfig.maxTokens());
        }
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
            Map.of(
                "role", "system",
                "content", "你是投资辅助报告解析器，只能基于输入数据生成JSON。低质量数据必须输出数据缺口和风险提示，禁止给出确定性收益承诺。"
            ),
            Map.of(
                "role", "user",
                "content", userPrompt
            )
        ));
        return payload;
    }

    /**
     * 构建发送给大模型的脱敏 Prompt。
     *
     * @param localReport 本地规则报告
     * @param command 投资分析命令
     * @return Prompt 文本
     * @author dz
     * @date 2026-06-24
     */
    private String prompt(InvestmentAnalysisReport localReport, GenerateInvestmentAnalysisCommand command) {
        return """
            请基于以下本地规则报告生成结构化投资分析 JSON。
            必须输出字段：investmentSummary、trend、investmentPlan、simulatedReturn、chartPayload、promptSnapshot。
            每个字段必须是 JSON 对象；不得输出 Markdown；不得编造缺失数据。
            如果 portfolioContext 不为空，investmentPlan 必须显式考虑当前 Mock 组合现金、总资产和持仓，
            actionType 只能是 BUY/SELL/REBALANCE/HOLD/SKIP 之一；现金不足时不得强行 BUY，应输出 HOLD、SELL、REBALANCE 或 SKIP，并解释原因。
            如果 portfolioContext.candidateProducts 不为空，必须从候选产品中选择可交易标的输出 productBizId/productCode/marketCode，
            或在 targetWeights 中输出目标权重；不得再以“无具体productBizId”为理由 HOLD。
            如果组合空仓、现金充足、数据质量为 HIGH_CONFIDENCE，且没有明确风险拒绝原因，应输出小额 BUY 或低权重 REBALANCE 作为可复盘 Mock 样本。
            若建议调仓，请在 investmentPlan.targetWeights 输出数组，元素包含 productBizId 和 targetWeight，targetWeight 总和不得超过 1。
            marketScope=%s
            themeCode=%s
            lookbackDays=%s
            portfolioContext=%s
            localDataQualityGate=%s
            localInvestmentSummary=%s
            localTrend=%s
            localInvestmentPlan=%s
            localSimulatedReturn=%s
            localChartPayload=%s
            """.formatted(
            localReport.marketScope(),
            command.themeCode(),
            command.lookbackDays(),
            command.portfolioContext(),
            localReport.dataQualityGate(),
            localReport.investmentSummary(),
            localReport.trend(),
            localReport.investmentPlan(),
            localReport.simulatedReturn(),
            localReport.chartPayload()
        );
    }

    /**
     * 记录投资报告模型调用开始。
     *
     * <p>审计上下文会把报告、闭环运行、组合上下文摘要和模型配置绑定到同一条调用流水，
     * 便于前端从报告或闭环节点反查模型输入。</p>
     *
     * @param callId 单次模型调用追踪标识
     * @param localReport 本地规则先生成的报告草稿
     * @param command 报告生成命令
     * @param modelConfig 模型运行时配置快照
     * @param endpoint 脱敏后的远端调用地址
     * @param userPrompt 发送给模型的用户提示词
     */
    private void startAudit(
        String callId,
        InvestmentAnalysisReport localReport,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig,
        String endpoint,
        String userPrompt
    ) {
        if (callAudits == null) {
            return;
        }
        Map<String, Object> portfolioContext = safeJsonMap(command.portfolioContext());
        callAudits.start(callId, "AUTO_INVESTMENT_REPORT_GENERATION", modelConfig, endpoint,
            sha256("INVESTMENT_REPORT_SYSTEM_PROMPT"), sha256(userPrompt),
            "你是投资辅助报告解析器，只能基于输入数据生成JSON。", userPrompt,
            AiModelCallAuditContext.builder()
                .businessType("INVESTMENT_REPORT")
                .businessBizId(localReport.bizId())
                .businessLabel("投资报告 " + localReport.bizId())
                .reportBizId(localReport.bizId())
                .runBizId(text(portfolioContext, "runBizId"))
                .runNo(text(portfolioContext, "runNo"))
                .scenarioCode("INVESTMENT_REPORT")
                .inputSummary(reportInputSummary(localReport, command, portfolioContext))
                .build());
    }

    /**
     * 记录投资报告模型调用成功。
     *
     * @param callId 单次模型调用追踪标识
     * @param httpStatus 远端 HTTP 状态
     * @param durationMs 调用耗时毫秒
     * @param responseHash 模型输出哈希
     * @param responseText 模型输出原文，审计服务会截断保存
     * @param outputSummary 投资建议、动作数量等可扩展输出摘要
     */
    private void succeedAudit(String callId, Integer httpStatus, long durationMs, String responseHash,
                              String responseText, Map<String, Object> outputSummary) {
        if (callAudits != null) {
            callAudits.succeed(callId, httpStatus, durationMs, responseHash, responseText, outputSummary);
        }
    }

    /**
     * 记录投资报告模型调用失败。
     *
     * @param callId 单次模型调用追踪标识
     * @param httpStatus 远端 HTTP 状态，未到达远端时为空
     * @param durationMs 调用耗时毫秒
     * @param responseHash 失败响应哈希
     * @param responseText 失败响应原文，审计服务会截断保存
     * @param errorCode 结构化错误编码
     * @param errorMessage 可展示失败原因
     */
    private void failAudit(String callId, Integer httpStatus, long durationMs, String responseHash,
                           String responseText, String errorCode, String errorMessage) {
        if (callAudits != null) {
            callAudits.fail(callId, httpStatus, durationMs, responseHash, responseText, errorCode, errorMessage);
        }
    }

    /**
     * 构造投资报告模型输入摘要。
     *
     * @param localReport 本地规则先生成的报告草稿
     * @param command 报告生成命令
     * @param portfolioContext Mock 组合、闭环运行和候选产品上下文
     * @return 可被模型调用中心结构化展示的输入摘要
     */
    private Map<String, Object> reportInputSummary(
        InvestmentAnalysisReport localReport,
        GenerateInvestmentAnalysisCommand command,
        Map<String, Object> portfolioContext
    ) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("requestId", localReport.requestId());
        summary.put("marketScope", localReport.marketScope());
        summary.put("themeCode", command.themeCode());
        summary.put("lookbackDays", command.lookbackDays());
        summary.put("dataQualityScore", localReport.dataQualityScore());
        summary.put("dataQualityGate", safeJsonMap(localReport.dataQualityGate()));
        summary.put("portfolioContext", portfolioContext);
        return summary;
    }

    /** 将 JSON 文本转为可展示摘要对象。 */
    private Map<String, Object> safeJsonMap(String value) {
        try {
            return Jsons.readObjectMapOrEmpty(value);
        } catch (Exception exception) {
            return value == null || value.isBlank() ? Map.of() : Map.of("raw", limit(value, 1200));
        }
    }

    /** 从摘要 Map 中读取文本字段。 */
    private String text(Map<String, Object> value, String key) {
        Object item = value == null ? null : value.get(key);
        return item == null ? null : String.valueOf(item);
    }

    /**
     * 合并远端模型输出和本地质量门禁。
     *
     * @param localReport 本地规则报告
     * @param modelConfig 模型配置
     * @param content 模型返回 JSON 文本
     * @param chatSnapshot 脱敏后的模型对话快照
     * @return 可落库报告
     * @author dz
     * @date 2026-06-24
     */
    private InvestmentAnalysisReport mergeRemoteOutput(
        InvestmentAnalysisReport localReport,
        AiModelRuntimeConfig modelConfig,
        String content,
        String chatSnapshot
    ) {
        JsonNode output = readModelOutputJson(content);
        return InvestmentAnalysisReport.builder()
            .bizId(localReport.bizId())
            .requestId(localReport.requestId())
            .providerCode(PROVIDER_CODE)
            .modelCode(modelConfig.modelCode())
            .marketScope(localReport.marketScope())
            .themeCode(localReport.themeCode())
            .themeName(localReport.themeName())
            .status("SUCCEEDED")
            .confidenceLevel(localReport.confidenceLevel())
            .dataQualityScore(localReport.dataQualityScore())
            .dataQualityGate(localReport.dataQualityGate())
            .investmentSummary(requiredObject(output, "investmentSummary"))
            .trend(requiredObject(output, "trend"))
            .investmentPlan(requiredObject(output, "investmentPlan"))
            .simulatedReturn(requiredObject(output, "simulatedReturn"))
            .chartPayload(requiredObject(output, "chartPayload"))
            .promptSnapshot(requiredObject(output, "promptSnapshot"))
            .chatSnapshot(chatSnapshot)
            .failureReason(null)
            .generatedAt(localReport.generatedAt())
            .createdAt(localReport.createdAt())
            .build();
    }

    /** 构建脱敏模型对话快照，供报告页展示真实输入输出证据。 */
    private String buildChatSnapshot(
        Map<String, Object> payload,
        String responseContent,
        String endpoint,
        int httpStatus,
        long durationMs
    ) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("providerCode", PROVIDER_CODE);
        snapshot.put("modelCode", String.valueOf(payload.getOrDefault("model", "")));
        snapshot.put("endpointHost", endpointHost(endpoint));
        snapshot.put("httpStatus", httpStatus);
        snapshot.put("durationMs", durationMs);
        snapshot.put("temperature", payload.get("temperature"));
        snapshot.put("maxTokens", payload.get("max_tokens"));
        snapshot.put("requestMessages", safeMessages(payload.get("messages")));
        snapshot.put("responseMessage", safeMessage("assistant", responseContent));
        return writeJson(snapshot);
    }

    /** 提取请求消息的角色和内容摘要，不返回完整长 Prompt 或任何密钥。 */
    private List<Map<String, Object>> safeMessages(Object messages) {
        if (!(messages instanceof List<?> items)) {
            return List.of();
        }
        return items.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(message -> safeMessage(
                String.valueOf(message.getOrDefault("role", "unknown")),
                String.valueOf(message.getOrDefault("content", ""))
            ))
            .toList();
    }

    /** 构建单条消息摘要。 */
    private Map<String, Object> safeMessage(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("contentPreview", limit(content, 1200));
        message.put("contentLength", textLength(content));
        return message;
    }

    /** 从模型地址中提取主机名，避免在报告中长期保存完整 endpoint。 */
    private String endpointHost(String endpoint) {
        try {
            return URI.create(endpoint).getHost();
        } catch (Exception exception) {
            return "UNKNOWN";
        }
    }

    private String resolveChatCompletionsUrl(String baseUrl) {
        String normalized = baseUrl == null || baseUrl.isBlank()
            ? "https://api.openai.com/v1"
            : baseUrl.replaceAll("/+$", "");
        return normalized.endsWith("/chat/completions")
            ? normalized
            : normalized + "/chat/completions";
    }

    private String extractContent(String responseBody) {
        JsonNode root = readJson(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型响应缺少 message.content");
        }
        return content.asText();
    }

    private String requiredObject(JsonNode output, String fieldName) {
        JsonNode value = output.get(fieldName);
        if (value == null || !value.isObject()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型输出缺少对象字段: " + fieldName);
        }
        return writeJson(value);
    }

    private JsonNode readJson(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型输出JSON格式不合法");
        }
    }

    /** 读取模型输出 JSON，允许模型在 JSON 外包裹说明文本或 Markdown 围栏。 */
    private JsonNode readModelOutputJson(String value) {
        return readJson(normalizeJsonObjectContent(value));
    }

    /** 规范化模型输出，允许从代码块或解释文本中提取第一个 JSON 对象。 */
    private String normalizeJsonObjectContent(String value) {
        String stripped = stripMarkdownFence(value);
        if (isJsonObject(stripped)) {
            return stripped;
        }
        String extracted = firstJsonObject(stripped);
        if (extracted != null && isJsonObject(extracted)) {
            log.warn(
                "投资分析模型输出包含非JSON包裹文本，已提取首个JSON对象: originalLength={}, extractedLength={}",
                textLength(value),
                textLength(extracted)
            );
            return extracted;
        }
        log.error("投资分析模型输出JSON格式不合法: contentPreview={}", limit(value, 1200));
        throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型输出JSON格式不合法");
    }

    /** 判断文本是否为 JSON 对象。 */
    private boolean isJsonObject(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            return objectMapper.readTree(value).isObject();
        } catch (JsonProcessingException exception) {
            return false;
        }
    }

    /** 去掉常见 Markdown 代码围栏。 */
    private String stripMarkdownFence(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineEnd < 0 || lastFence <= firstLineEnd) {
            return trimmed;
        }
        return trimmed.substring(firstLineEnd + 1, lastFence).trim();
    }

    /** 从文本中提取第一个括号平衡的 JSON 对象。 */
    private String firstJsonObject(String value) {
        if (value == null) {
            return null;
        }
        int start = value.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return value.substring(start, index + 1).trim();
                }
            }
        }
        return null;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "OpenAI兼容模型请求JSON序列化失败");
        }
    }

    /** 计算文本长度，避免日志输出完整模型响应。 */
    private int textLength(String value) {
        return value == null ? 0 : value.length();
    }

    /** 截断远端错误响应，保留排查线索并避免日志过长。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 把常见网关错误转换成可行动的业务错误。 */
    private String remoteFailureMessage(int httpStatus, String responseBody) {
        if (httpStatus == 524) {
            return "OpenAI兼容模型网关超时: HTTP 524。通常是中转或上游模型长时间未响应，请降低报告主题数量/输出token，或切换更稳定的模型通道。body="
                + limit(responseBody, 300);
        }
        if (httpStatus == 429) {
            return "OpenAI兼容模型限流: HTTP 429。请降低定时任务频率或更换模型配额。body=" + limit(responseBody, 300);
        }
        if (httpStatus >= 500) {
            return "OpenAI兼容模型服务端异常: HTTP " + httpStatus + "。请稍后重试或切换模型通道。body=" + limit(responseBody, 300);
        }
        return "OpenAI兼容模型调用失败: HTTP " + httpStatus + ", body=" + limit(responseBody, 500);
    }

    /** 判断文本是否为空。 */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 计算模型远端调用耗时毫秒。 */
    private long elapsedMs(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }

    /** 计算文本 SHA-256 摘要，日志只记录摘要用于关联模型输入输出。 */
    private String sha256(String value) {
        if (value == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            return "SHA256_UNAVAILABLE";
        }
    }
}
