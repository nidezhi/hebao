package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;

/** 可插拔投资分析提供方端口。 */
public interface InvestmentAnalysisProvider {
    /** 判断当前提供方是否支持请求的 providerCode。 */
    boolean supports(String providerCode);

    /** 基于平台数据生成结构化投资分析报告。 */
    InvestmentAnalysisReport analyze(String requestId, GenerateInvestmentAnalysisCommand command);
}
