package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.InvestmentThemeOption;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentThemeSnapshotEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.InvestmentThemeSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 投资主题快照仓储实现。 */
@Repository
@RequiredArgsConstructor
public class InvestmentThemeSnapshotStoreImpl implements InvestmentThemeSnapshotStore {
    private final InvestmentThemeSnapshotMapper mapper;

    /** 保存主题分析快照。 */
    @Override
    public InvestmentThemeSnapshot save(InvestmentThemeSnapshot snapshot) {
        mapper.insert(InvestmentThemeSnapshotEntity.builder()
            .bizId(snapshot.bizId())
            .taskCode(snapshot.taskCode())
            .snapshotType(snapshot.snapshotType())
            .themeCode(snapshot.themeCode())
            .themeName(snapshot.themeName())
            .marketScope(snapshot.marketScope())
            .windowMinutes(snapshot.windowMinutes())
            .sampleCount(snapshot.sampleCount())
            .returnRate(snapshot.returnRate())
            .momentumScore(snapshot.momentumScore())
            .heatScore(snapshot.heatScore())
            .topProductBizId(snapshot.topProductBizId())
            .metrics(snapshot.metrics())
            .snapshotTime(snapshot.snapshotTime())
            .createdAt(snapshot.createdAt())
            .build());
        return snapshot;
    }

    /** 根据筛选条件分页查询主题快照。 */
    @Override
    public PageResult<InvestmentThemeSnapshot> search(InvestmentThemeSnapshotSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<InvestmentThemeSnapshot> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<InvestmentThemeSnapshot>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 从真实快照中查询去重后的投资主题选择器选项。 */
    @Override
    public PageResult<InvestmentThemeOption> searchThemeOptions(String keyword, String marketScope, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;
        List<InvestmentThemeOption> items = mapper.searchThemeOptions(
                blankToNull(keyword),
                blankToNull(marketScope),
                offset,
                safeSize
            ).stream()
            .map(this::toThemeOption)
            .toList();
        long total = mapper.countThemeOptions(blankToNull(keyword), blankToNull(marketScope));
        return PageResult.<InvestmentThemeOption>builder()
            .items(items)
            .total(total)
            .page(safePage)
            .size(safeSize)
            .totalPages((int) Math.ceil((double) total / safeSize))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "taskCode" -> "s.task_code";
            case "snapshotType" -> "s.snapshot_type";
            case "themeCode" -> "s.theme_code";
            case "returnRate" -> "s.return_rate";
            case "momentumScore" -> "s.momentum_score";
            case "heatScore" -> "s.heat_score";
            case "createdAt" -> "s.created_at";
            default -> "s.snapshot_time";
        };
    }

    /** 将持久化实体转换为领域对象。 */
    private InvestmentThemeSnapshot toDomain(InvestmentThemeSnapshotEntity entity) {
        return InvestmentThemeSnapshot.builder()
            .bizId(entity.getBizId())
            .taskCode(entity.getTaskCode())
            .snapshotType(entity.getSnapshotType())
            .themeCode(entity.getThemeCode())
            .themeName(entity.getThemeName())
            .marketScope(entity.getMarketScope())
            .windowMinutes(entity.getWindowMinutes())
            .sampleCount(entity.getSampleCount())
            .returnRate(entity.getReturnRate())
            .momentumScore(entity.getMomentumScore())
            .heatScore(entity.getHeatScore())
            .topProductBizId(entity.getTopProductBizId())
            .metrics(entity.getMetrics())
            .snapshotTime(entity.getSnapshotTime())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    /** 将 SQL 投影转换为主题选项。 */
    private InvestmentThemeOption toThemeOption(Map<String, Object> row) {
        return InvestmentThemeOption.builder()
            .themeCode((String) row.get("themeCode"))
            .themeName((String) row.get("themeName"))
            .marketScope((String) row.get("marketScope"))
            .latestSnapshotType((String) row.get("latestSnapshotType"))
            .latestSnapshotTime((LocalDateTime) row.get("latestSnapshotTime"))
            .sampleCount(numberToInt(row.get("sampleCount")))
            .returnRate((BigDecimal) row.get("returnRate"))
            .momentumScore((BigDecimal) row.get("momentumScore"))
            .heatScore((BigDecimal) row.get("heatScore"))
            .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int numberToInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
