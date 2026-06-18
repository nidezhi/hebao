package com.example.dzcom.infrastructure.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.application.service.ai.InvestmentAnalysisProvider;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * OpenAI 兼容协议的模拟投资分析 Provider。
 *
 * <p>当前用于验证数据库模型配置、Provider 选择和 API Key 注入链路。
 * 当 {@code mockEnabled=true} 时复用本地规则生成结构化报告，不发起外部网络请求。</p>
 */
@Component
@RequiredArgsConstructor
public class MockOpenAiCompatibleInvestmentAnalysisProvider
    implements InvestmentAnalysisProvider {

    private static final String PROVIDER_CODE = "OPENAI_COMPATIBLE";

    private final LocalRuleInvestmentAnalysisProvider localRuleProvider;

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
     * 使用已注入密钥的 OpenAI 兼容模拟配置生成报告。
     *
     * @param requestId 单次分析请求追踪标识
     * @param command 市场范围、主题和模拟资金等业务参数
     * @param modelConfig 数据库配置与外部 API Key 组成的运行配置
     * @return 标记为 OPENAI_COMPATIBLE 模型生成的结构化报告
     * @throws BusinessException 当模型未开启 mock 模式时抛出
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public InvestmentAnalysisReport analyze(
        String requestId,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    ) {
        if (!modelConfig.mockEnabled()) {
            throw new BusinessException(
                HttpStatus.NOT_IMPLEMENTED,
                "OpenAI兼容模型当前仅支持mockEnabled=true"
            );
        }
        InvestmentAnalysisReport localReport = localRuleProvider.analyze(
            requestId,
            command,
            modelConfig
        );
        return InvestmentAnalysisReport.builder()
            .bizId(localReport.bizId())
            .requestId(localReport.requestId())
            .providerCode(PROVIDER_CODE)
            .modelCode(modelConfig.modelCode())
            .marketScope(localReport.marketScope())
            .themeCode(localReport.themeCode())
            .themeName(localReport.themeName())
            .status(localReport.status())
            .investmentSummary(localReport.investmentSummary())
            .trend(localReport.trend())
            .investmentPlan(localReport.investmentPlan())
            .simulatedReturn(localReport.simulatedReturn())
            .chartPayload(localReport.chartPayload())
            .promptSnapshot(localReport.promptSnapshot())
            .failureReason(localReport.failureReason())
            .generatedAt(localReport.generatedAt())
            .createdAt(localReport.createdAt())
            .build();
    }
}
