package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.InvestmentAnalysisReport;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.InvestmentAnalysisReportEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.InvestmentAnalysisReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 投资分析报告仓储实现。 */
@Repository
@RequiredArgsConstructor
public class InvestmentAnalysisReportStoreImpl implements InvestmentAnalysisReportStore {
    private final InvestmentAnalysisReportMapper mapper;

    /** 保存投资分析报告。 */
    @Override
    public InvestmentAnalysisReport save(InvestmentAnalysisReport report) {
        mapper.insert(toEntity(report));
        return report;
    }

    /** 分页查询投资分析报告。 */
    @Override
    public PageResult<InvestmentAnalysisReport> search(InvestmentAnalysisReportSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<InvestmentAnalysisReport> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<InvestmentAnalysisReport>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "providerCode" -> "r.provider_code";
            case "modelCode" -> "r.model_code";
            case "themeCode" -> "r.theme_code";
            case "status" -> "r.status";
            case "confidenceLevel" -> "r.confidence_level";
            case "dataQualityScore" -> "r.data_quality_score";
            case "createdAt" -> "r.created_at";
            default -> "r.generated_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private InvestmentAnalysisReportEntity toEntity(InvestmentAnalysisReport report) {
        return InvestmentAnalysisReportEntity.builder()
            .bizId(report.bizId())
            .requestId(report.requestId())
            .providerCode(report.providerCode())
            .modelCode(report.modelCode())
            .marketScope(report.marketScope())
            .themeCode(report.themeCode())
            .themeName(report.themeName())
            .status(report.status())
            .confidenceLevel(report.confidenceLevel())
            .dataQualityScore(report.dataQualityScore())
            .dataQualityGate(report.dataQualityGate())
            .investmentSummary(report.investmentSummary())
            .trend(report.trend())
            .investmentPlan(report.investmentPlan())
            .simulatedReturn(report.simulatedReturn())
            .chartPayload(report.chartPayload())
            .promptSnapshot(report.promptSnapshot())
            .failureReason(report.failureReason())
            .generatedAt(report.generatedAt())
            .createdAt(report.createdAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private InvestmentAnalysisReport toDomain(InvestmentAnalysisReportEntity entity) {
        return InvestmentAnalysisReport.builder()
            .bizId(entity.getBizId())
            .requestId(entity.getRequestId())
            .providerCode(entity.getProviderCode())
            .modelCode(entity.getModelCode())
            .marketScope(entity.getMarketScope())
            .themeCode(entity.getThemeCode())
            .themeName(entity.getThemeName())
            .status(entity.getStatus())
            .confidenceLevel(entity.getConfidenceLevel())
            .dataQualityScore(entity.getDataQualityScore())
            .dataQualityGate(entity.getDataQualityGate())
            .investmentSummary(entity.getInvestmentSummary())
            .trend(entity.getTrend())
            .investmentPlan(entity.getInvestmentPlan())
            .simulatedReturn(entity.getSimulatedReturn())
            .chartPayload(entity.getChartPayload())
            .promptSnapshot(entity.getPromptSnapshot())
            .failureReason(entity.getFailureReason())
            .generatedAt(entity.getGeneratedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
