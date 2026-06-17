package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;

/** 投资分析报告仓储端口。 */
public interface InvestmentAnalysisReportStore {
    /** 保存投资分析报告。 */
    InvestmentAnalysisReport save(InvestmentAnalysisReport report);

    /** 分页查询投资分析报告。 */
    PageResult<InvestmentAnalysisReport> search(InvestmentAnalysisReportSearchCriteria criteria);
}
