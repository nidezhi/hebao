package com.example.dzcom.application.service.task;

import com.alibaba.fastjson2.JSON;
import com.example.dzcom.application.command.market.DiscoverDataSourcesCommand;
import com.example.dzcom.application.dto.market.DataSourceDiscoveryCandidateView;
import com.example.dzcom.application.dto.market.DataSourceDiscoveryView;
import com.example.dzcom.application.service.market.DataSourceGovernanceApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** AI 数据源发现任务，负责按模型挂靠和 Skill 产出候选来源、字段映射和采集建议。 */
@Service
@RequiredArgsConstructor
public class AiDataSourceDiscoveryTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AI_DATA_SOURCE_DISCOVERY";

    private final DataSourceGovernanceApplicationService dataSources;

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
        DataSourceDiscoveryView discovery = dataSources.discover(DiscoverDataSourcesCommand.builder()
            .marketScope(TaskParameterParser.string(parameters, "marketScope", TaskParameterParser.CN_MAINLAND))
            .assetClass(TaskParameterParser.string(parameters, "assetClass", "MULTI_ASSET"))
            .dataTypes(TaskParameterParser.string(parameters, "dataTypes",
                "MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY"))
            .topicKeywords(TaskParameterParser.string(parameters, "topicKeywords", ""))
            .preferredTrustLevels(TaskParameterParser.string(parameters, "preferredTrustLevels", "L1,L2,L3,L4"))
            .candidateLimit(TaskParameterParser.positiveInt(parameters, "candidateLimit", 8))
            .environment(TaskParameterParser.string(parameters, "environment",
                AiModelBindingApplicationServiceDefault.DEFAULT_ENVIRONMENT))
            .includeDisabledCandidates(TaskParameterParser.bool(parameters, "includeDisabledCandidates", true))
            .build());
        List<String> candidateCodes = discovery.candidates().stream()
            .map(DataSourceDiscoveryCandidateView::sourceCode)
            .toList();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("scenarioCode", discovery.scenarioCode());
        summary.put("modelCode", discovery.modelCode());
        summary.put("skillCode", discovery.skillCode());
        summary.put("skillVersion", discovery.skillVersion());
        summary.put("candidateCount", discovery.candidates().size());
        summary.put("candidateCodes", candidateCodes);
        summary.put("reviewPolicy", discovery.reviewPolicy());
        return JSON.toJSONString(summary);
    }

    /** 避免 task 包直接依赖 AI 服务常量造成循环语义。 */
    private static final class AiModelBindingApplicationServiceDefault {
        private static final String DEFAULT_ENVIRONMENT = "DEFAULT";
    }
}
