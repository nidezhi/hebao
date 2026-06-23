package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.BacktestResult;
import com.example.dzcom.domain.repository.ai.BacktestResultSearchCriteria;
import com.example.dzcom.domain.repository.ai.BacktestResultStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.BacktestResultEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.BacktestResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 回测结果仓储实现。 */
@Repository
@RequiredArgsConstructor
public class BacktestResultStoreImpl implements BacktestResultStore {
    private final BacktestResultMapper mapper;

    /** 保存回测结果。 */
    @Override
    public BacktestResult save(BacktestResult result) {
        mapper.save(toEntity(result));
        return result;
    }

    /** 按业务 ID 查询回测结果。 */
    @Override
    public Optional<BacktestResult> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 分页查询回测结果。 */
    @Override
    public PageResult<BacktestResult> search(BacktestResultSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<BacktestResult> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<BacktestResult>builder()
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
            case "strategyCode" -> "b.strategy_code";
            case "strategyVersion" -> "b.strategy_version";
            case "status" -> "b.status";
            case "startDate" -> "b.start_date";
            case "endDate" -> "b.end_date";
            default -> "b.created_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private BacktestResultEntity toEntity(BacktestResult result) {
        return BacktestResultEntity.builder()
            .bizId(result.bizId())
            .ownerUserBizId(result.ownerUserBizId())
            .strategyCode(result.strategyCode())
            .strategyVersion(result.strategyVersion())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .initialCapital(result.initialCapital())
            .benchmarkCode(result.benchmarkCode())
            .parameters(result.parameters())
            .metrics(result.metrics())
            .resultUri(result.resultUri())
            .status(result.status())
            .failureReason(result.failureReason())
            .startedAt(result.startedAt())
            .completedAt(result.completedAt())
            .createdAt(result.createdAt())
            .updatedAt(result.updatedAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private BacktestResult toDomain(BacktestResultEntity entity) {
        return BacktestResult.builder()
            .bizId(entity.getBizId())
            .ownerUserBizId(entity.getOwnerUserBizId())
            .strategyCode(entity.getStrategyCode())
            .strategyVersion(entity.getStrategyVersion())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .initialCapital(entity.getInitialCapital())
            .benchmarkCode(entity.getBenchmarkCode())
            .parameters(entity.getParameters())
            .metrics(entity.getMetrics())
            .resultUri(entity.getResultUri())
            .status(entity.getStatus())
            .failureReason(entity.getFailureReason())
            .startedAt(entity.getStartedAt())
            .completedAt(entity.getCompletedAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
