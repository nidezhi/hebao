package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.application.service.task.TaskParameterParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** 投资分析报告生成与查询用例。 */
@Service
@RequiredArgsConstructor
public class InvestmentAnalysisApplicationService {
    private static final String DEFAULT_MODEL_CODE = "local-rule-analysis";
    private static final Set<String> SORTS =
        Set.of("generatedAt", "createdAt", "providerCode", "modelCode", "themeCode", "status");

    private final List<InvestmentAnalysisProvider> providers;
    private final AiModelStore models;
    private final AiModelRuntimeConfigResolver modelConfigResolver;
    private final InvestmentAnalysisReportStore reports;
    private final IdGenerator ids;

    /**
     * 根据 ACTIVE 模型配置选择 Provider 并生成投资分析报告。
     *
     * <p>模型普通参数来自数据库，API Key 通过模型配置中的 secretRef 从外部密钥
     * 解析器注入。请求 providerCode 仅用于校验，不作为实际 Provider 来源。</p>
     *
     * @param command 模型编码、市场范围、主题和模拟资金等分析参数
     * @return 已保存的投资分析报告
     * @throws BusinessException 当模型、Provider 或密钥配置不存在时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Transactional
    public InvestmentAnalysisReport generate(GenerateInvestmentAnalysisCommand command) {
        String modelCode = resolveModelCode(command.modelCode());
        AiModel model = models.findActiveByCode(modelCode)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "未找到启用的AI模型配置: " + modelCode
            ));
        validateRequestedProvider(command.providerCode(), model.provider());

        AiModelRuntimeConfig runtimeConfig = modelConfigResolver.resolve(model);
        InvestmentAnalysisProvider provider = providers.stream()
            .filter(candidate -> candidate.supports(model.provider()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "投资分析提供方不存在"));
        InvestmentAnalysisReport report = provider.analyze(
            ids.newBizId(),
            command,
            runtimeConfig
        );
        return reports.save(report);
    }

    /**
     * 分页查询已生成的投资分析报告。
     *
     * @param marketScope 市场范围，空值默认中国大陆
     * @param themeCode 投资主题筛选条件
     * @param providerCode 分析提供方筛选条件
     * @param status 报告状态筛选条件
     * @param pageQuery 分页和排序参数
     * @return 投资分析报告分页结果
     * @author dz
     * @date 2026-06-18
     */
    @Transactional(readOnly = true)
    public PageResult<InvestmentAnalysisReport> reports(String marketScope, String themeCode,
                                                        String providerCode, String status,
                                                        PageQuery pageQuery) {
        return reports.search(new InvestmentAnalysisReportSearchCriteria(
            marketScope == null || marketScope.isBlank() ? TaskParameterParser.CN_MAINLAND : marketScope,
            themeCode,
            providerCode,
            status,
            pageQuery.page(),
            pageQuery.size(),
            pageQuery.safeSort(SORTS, "generatedAt"),
            "asc".equals(pageQuery.direction())
        ));
    }

    /**
     * 解析请求模型编码，空值使用本地规则默认模型。
     *
     * @param modelCode 请求指定模型编码
     * @return 可用于查询 ACTIVE 模型的稳定编码
     * @author dz
     * @date 2026-06-18
     */
    private String resolveModelCode(String modelCode) {
        return modelCode == null || modelCode.isBlank()
            ? DEFAULT_MODEL_CODE
            : modelCode;
    }

    /**
     * 校验请求指定的 Provider 与模型注册信息一致。
     *
     * @param requestedProvider 请求中的可选 Provider 编码
     * @param configuredProvider 模型表中实际 Provider 编码
     * @throws BusinessException 当两者均存在但不一致时抛出
     * @author dz
     * @date 2026-06-18
     */
    private void validateRequestedProvider(
        String requestedProvider,
        String configuredProvider
    ) {
        if (requestedProvider == null || requestedProvider.isBlank()) {
            return;
        }
        if (!requestedProvider.equals(configuredProvider)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "请求Provider与模型配置不一致"
            );
        }
    }
}
