package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;

/** 可插拔投资分析提供方端口。 */
public interface InvestmentAnalysisProvider {
    /** 判断当前提供方是否支持请求的 providerCode。 */
    boolean supports(String providerCode);

    /**
     * 基于平台数据和模型运行配置生成结构化投资分析报告。
     *
     * @param requestId 单次分析请求追踪标识
     * @param command 市场范围、主题和模拟资金等业务参数
     * @param modelConfig 从 ACTIVE 模型记录和外部密钥解析得到的运行配置
     * @return 可落库的结构化投资分析报告
     * @author dz
     * @date 2026-06-18
     */
    InvestmentAnalysisReport analyze(
        String requestId,
        GenerateInvestmentAnalysisCommand command,
        AiModelRuntimeConfig modelConfig
    );
}
