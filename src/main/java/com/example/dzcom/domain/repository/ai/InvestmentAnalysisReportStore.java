package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;

import java.util.Optional;

/** 投资分析报告仓储端口。 */
public interface InvestmentAnalysisReportStore {
    /** 保存投资分析报告。 */
    InvestmentAnalysisReport save(InvestmentAnalysisReport report);

    /** 根据报告业务标识查询投资分析报告。 */
    Optional<InvestmentAnalysisReport> findByBizId(String bizId);

    /** 分页查询投资分析报告。 */
    PageResult<InvestmentAnalysisReport> search(InvestmentAnalysisReportSearchCriteria criteria);

    /** 查询最近生成的投资分析报告，用于自动 Prompt 治理任务复盘真实报告样本。 */
    PageResult<InvestmentAnalysisReport> latest(int size);
}
