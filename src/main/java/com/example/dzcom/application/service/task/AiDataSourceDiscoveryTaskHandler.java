package com.example.dzcom.application.service.task;

import com.alibaba.fastjson2.JSON;
import com.example.dzcom.application.command.market.SaveDataSourceCommand;
import com.example.dzcom.application.command.market.DiscoverDataSourcesCommand;
import com.example.dzcom.application.dto.market.DataSourceDiscoveryCandidateView;
import com.example.dzcom.application.dto.market.DataSourceDiscoveryView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.application.service.market.DataSourceGovernanceApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** AI 数据源发现任务，负责按模型挂靠和 Skill 产出候选来源、字段映射和采集建议。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiDataSourceDiscoveryTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AI_DATA_SOURCE_DISCOVERY";

    private final DataSourceGovernanceApplicationService dataSources;
    private final CurrentOperatorProvider currentOperator;

    /**
     * 判断当前处理器是否支持 AI 数据源发现任务。
     *
     * @param taskType 任务类型
     * @return 类型为 AI_DATA_SOURCE_DISCOVERY 时返回 true
     * @author dz
     * @date 2026-06-26
     */
    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    /**
     * 执行 AI 数据源发现并返回前端可审计摘要。
     *
     * <p>任务只生成候选和 Prompt 预览，不直接启用正式数据源或供应商授权。
     * 闭环编排可以消费本任务结果作为“数据源治理已运行”的证据，前端再决定是否保存候选。</p>
     *
     * @param event 任务触发事件
     * @return 数据源发现摘要 JSON
     * @author dz
     * @date 2026-06-26
     */
    @Override
    public String execute(InvestmentTaskEvent event) {
        Map<String, String> parameters = event.parameters() == null ? Map.of() : event.parameters();
        log.info(
            "AI数据源发现任务开始: taskCode={}, eventId={}, triggerSource={}, environment={}, marketScope={}, assetClass={}, dataTypes={}, collectionDirection={}, skillCode={}, candidateLimit={}, autoRegisterCandidates={}, autoEnableCandidates={}",
            event.taskCode(),
            event.eventId(),
            event.triggerSource(),
            TaskParameterParser.string(parameters, "environment", AiModelBindingApplicationServiceDefault.DEFAULT_ENVIRONMENT),
            TaskParameterParser.string(parameters, "marketScope", TaskParameterParser.CN_MAINLAND),
            TaskParameterParser.string(parameters, "assetClass", "MULTI_ASSET"),
            TaskParameterParser.string(parameters, "dataTypes", "MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY"),
            TaskParameterParser.string(parameters, "collectionDirection", "MULTI_SOURCE"),
            TaskParameterParser.string(parameters, "skillCode", ""),
            TaskParameterParser.positiveInt(parameters, "candidateLimit", 8),
            TaskParameterParser.bool(parameters, "autoRegisterCandidates", true),
            TaskParameterParser.bool(parameters, "autoEnableCandidates", false)
        );
        DataSourceDiscoveryView discovery = dataSources.discover(DiscoverDataSourcesCommand.builder()
            .marketScope(TaskParameterParser.string(parameters, "marketScope", TaskParameterParser.CN_MAINLAND))
            .assetClass(TaskParameterParser.string(parameters, "assetClass", "MULTI_ASSET"))
            .dataTypes(TaskParameterParser.string(parameters, "dataTypes",
                "MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY"))
            .topicKeywords(TaskParameterParser.string(parameters, "topicKeywords", ""))
            .collectionDirection(TaskParameterParser.string(parameters, "collectionDirection", "MULTI_SOURCE"))
            .skillCode(TaskParameterParser.string(parameters, "skillCode", ""))
            .preferredTrustLevels(TaskParameterParser.string(parameters, "preferredTrustLevels", "L1,L2,L3,L4"))
            .candidateLimit(TaskParameterParser.positiveInt(parameters, "candidateLimit", 8))
            .environment(TaskParameterParser.string(parameters, "environment",
                AiModelBindingApplicationServiceDefault.DEFAULT_ENVIRONMENT))
            .includeDisabledCandidates(TaskParameterParser.bool(parameters, "includeDisabledCandidates", true))
            .build());
        List<String> candidateCodes = discovery.candidates().stream()
            .map(DataSourceDiscoveryCandidateView::sourceCode)
            .toList();
        List<String> registeredCodes = registerCandidates(parameters, discovery);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("scenarioCode", discovery.scenarioCode());
        summary.put("modelCode", discovery.modelCode());
        summary.put("skillCode", discovery.skillCode());
        summary.put("skillVersion", discovery.skillVersion());
        summary.put("collectionDirection", TaskParameterParser.string(parameters, "collectionDirection", "MULTI_SOURCE"));
        summary.put("dataTypes", discovery.dataTypes());
        summary.put("candidateCount", discovery.candidates().size());
        summary.put("candidateCodes", candidateCodes);
        summary.put("registeredCodes", registeredCodes);
        summary.put("autoEnableCandidates", TaskParameterParser.bool(parameters, "autoEnableCandidates", false));
        summary.put("reviewPolicy", discovery.reviewPolicy());
        log.info(
            "AI数据源发现任务完成: taskCode={}, eventId={}, scenarioCode={}, modelCode={}, skillCode={}, skillVersion={}, candidateCount={}, registeredCount={}",
            event.taskCode(),
            event.eventId(),
            discovery.scenarioCode(),
            discovery.modelCode(),
            discovery.skillCode(),
            discovery.skillVersion(),
            discovery.candidates().size(),
            registeredCodes.size()
        );
        return JSON.toJSONString(summary);
    }

    /** 按任务配置把模型候选沉淀到数据源池，默认不启用候选。 */
    private List<String> registerCandidates(Map<String, String> parameters, DataSourceDiscoveryView discovery) {
        if (!TaskParameterParser.bool(parameters, "autoRegisterCandidates", true)) {
            return List.of();
        }
        boolean enabled = TaskParameterParser.bool(parameters, "autoEnableCandidates", false);
        CurrentOperator operator = new CurrentOperator(
            TaskParameterParser.string(parameters, "operatorBizId", "SYSTEM_AI_DATA_SOURCE_DISCOVERY"),
            "SYSTEM_TASK",
            Set.of("ADMIN"),
            Set.of("INVESTMENT_TASK_WRITE", "DATA_SOURCE_WRITE")
        );
        return currentOperator.callAs(operator, () -> discovery.candidates().stream()
            .map(candidate -> dataSources.saveDiscoveredCandidate(SaveDataSourceCommand.builder()
                .sourceCode(candidate.sourceCode())
                .sourceName(candidate.sourceName())
                .sourceType(candidate.sourceType())
                .trustLevel(candidate.trustLevel())
                .baseUrl(candidate.baseUrl())
                .enabled(enabled && !candidate.requiresReview())
                .fetchFrequency(candidate.fetchFrequency())
                .owner(candidate.owner())
                .description(candidateDescription(discovery, candidate))
                .build(), enabled && !candidate.requiresReview()).sourceCode())
            .toList());
    }

    /** 生成带有 Skill、采集计划和质量策略的候选数据源说明。 */
    private String candidateDescription(DataSourceDiscoveryView discovery, DataSourceDiscoveryCandidateView candidate) {
        Map<String, Object> description = new LinkedHashMap<>();
        description.put("summary", candidate.description());
        description.put("skillCode", discovery.skillCode());
        description.put("skillVersion", discovery.skillVersion());
        description.put("recommendedTaskType", candidate.recommendedTaskType());
        description.put("collectionPlan", candidate.collectionPlan());
        description.put("qualityPolicy", candidate.qualityPolicy());
        description.put("confidence", candidate.confidence());
        description.put("requiresReview", candidate.requiresReview());
        return limit(JSON.toJSONString(description), 512);
    }

    /** 限制写入数据源说明的长度，完整候选内容保留在任务执行摘要和发现响应中。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 避免 task 包直接依赖 AI 服务常量造成循环语义。 */
    private static final class AiModelBindingApplicationServiceDefault {
        private static final String DEFAULT_ENVIRONMENT = "DEFAULT";
    }
}
