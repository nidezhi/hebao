package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
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
    private static final Set<String> SORTS =
        Set.of("generatedAt", "createdAt", "providerCode", "modelCode", "themeCode", "status");

    private final List<InvestmentAnalysisProvider> providers;
    private final InvestmentAnalysisReportStore reports;
    private final IdGenerator ids;

    /** 调用匹配的分析提供方生成并保存报告。 */
    @Transactional
    public InvestmentAnalysisReport generate(GenerateInvestmentAnalysisCommand command) {
        String providerCode = command.providerCode() == null || command.providerCode().isBlank()
            ? "LOCAL_RULE"
            : command.providerCode();
        InvestmentAnalysisProvider provider = providers.stream()
            .filter(candidate -> candidate.supports(providerCode))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "投资分析提供方不存在"));
        InvestmentAnalysisReport report = provider.analyze(ids.newBizId(), command);
        return reports.save(report);
    }

    /** 分页查询已生成的投资分析报告。 */
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
}
