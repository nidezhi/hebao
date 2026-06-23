package com.example.dzcom.infrastructure.persistence.repository.market;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.repository.market.DataSourceSearchCriteria;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.example.dzcom.infrastructure.persistence.entity.market.DataSourceEntity;
import com.example.dzcom.infrastructure.persistence.entity.market.DataSourceHealthEntity;
import com.example.dzcom.infrastructure.persistence.entity.market.DataQualitySnapshotEntity;
import com.example.dzcom.infrastructure.persistence.mapper.market.DataSourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 数据源治理仓储实现。 */
@Repository
@RequiredArgsConstructor
public class DataSourceStoreImpl implements DataSourceStore {
    private final DataSourceMapper mapper;

    /** 新增或更新数据源注册信息。 */
    @Override
    public DataSource save(DataSource source) {
        mapper.save(toEntity(source));
        return source;
    }

    /** 保存数据源健康状态。 */
    @Override
    public DataSourceHealth saveHealth(DataSourceHealth health) {
        mapper.saveHealth(toEntity(health));
        return health;
    }

    /** 保存数据质量快照。 */
    @Override
    public DataQualitySnapshot saveQualitySnapshot(DataQualitySnapshot snapshot) {
        mapper.insertQualitySnapshot(toEntity(snapshot));
        return snapshot;
    }

    /** 按数据源编码查询数据源。 */
    @Override
    public Optional<DataSource> findBySourceCode(String sourceCode) {
        return Optional.ofNullable(mapper.selectBySourceCode(sourceCode)).map(this::toDomain);
    }

    /** 按数据源编码查询健康状态。 */
    @Override
    public Optional<DataSourceHealth> findHealthBySourceCode(String sourceCode) {
        return Optional.ofNullable(mapper.selectHealthBySourceCode(sourceCode)).map(this::toDomain);
    }

    /** 查询指定数据源的质量快照。 */
    @Override
    public List<DataQualitySnapshot> findQualitySnapshots(String sourceCode, String dataType, int limit) {
        return mapper.selectQualitySnapshots(sourceCode, dataType, limit).stream()
            .map(this::toDomain)
            .toList();
    }

    /** 分页查询数据源注册信息。 */
    @Override
    public PageResult<DataSource> search(DataSourceSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<DataSource> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<DataSource>builder()
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
            case "sourceCode" -> "s.source_code";
            case "sourceName" -> "s.source_name";
            case "sourceType" -> "s.source_type";
            case "trustLevel" -> "s.trust_level";
            case "enabled" -> "s.enabled";
            default -> "s.updated_at";
        };
    }

    /** 将数据源领域对象转换为持久化实体。 */
    private DataSourceEntity toEntity(DataSource source) {
        return DataSourceEntity.builder()
            .bizId(source.bizId())
            .sourceCode(source.sourceCode())
            .sourceName(source.sourceName())
            .sourceType(source.sourceType())
            .trustLevel(source.trustLevel())
            .baseUrl(source.baseUrl())
            .enabled(source.enabled())
            .fetchFrequency(source.fetchFrequency())
            .owner(source.owner())
            .description(source.description())
            .createdAt(source.createdAt())
            .updatedAt(source.updatedAt())
            .createdBy(source.createdBy())
            .updatedBy(source.updatedBy())
            .build();
    }

    /** 将健康状态领域对象转换为持久化实体。 */
    private DataSourceHealthEntity toEntity(DataSourceHealth health) {
        return DataSourceHealthEntity.builder()
            .bizId(health.bizId())
            .sourceCode(health.sourceCode())
            .lastSuccessAt(health.lastSuccessAt())
            .lastFailureAt(health.lastFailureAt())
            .successRate(health.successRate())
            .avgLatencyMs(health.avgLatencyMs())
            .failureReason(health.failureReason())
            .sampleCount(health.sampleCount())
            .updatedAt(health.updatedAt())
            .build();
    }

    /** 将质量快照领域对象转换为持久化实体。 */
    private DataQualitySnapshotEntity toEntity(DataQualitySnapshot snapshot) {
        return DataQualitySnapshotEntity.builder()
            .bizId(snapshot.bizId())
            .sourceCode(snapshot.sourceCode())
            .dataType(snapshot.dataType())
            .qualityScore(snapshot.qualityScore())
            .missingRate(snapshot.missingRate())
            .duplicateRate(snapshot.duplicateRate())
            .freshnessScore(snapshot.freshnessScore())
            .sampleCount(snapshot.sampleCount())
            .snapshotTime(snapshot.snapshotTime())
            .detail(snapshot.detail())
            .createdAt(snapshot.createdAt())
            .build();
    }

    /** 将数据源持久化实体转换为领域对象。 */
    private DataSource toDomain(DataSourceEntity entity) {
        return DataSource.builder()
            .bizId(entity.getBizId())
            .sourceCode(entity.getSourceCode())
            .sourceName(entity.getSourceName())
            .sourceType(entity.getSourceType())
            .trustLevel(entity.getTrustLevel())
            .baseUrl(entity.getBaseUrl())
            .enabled(entity.isEnabled())
            .fetchFrequency(entity.getFetchFrequency())
            .owner(entity.getOwner())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }

    /** 将健康状态持久化实体转换为领域对象。 */
    private DataSourceHealth toDomain(DataSourceHealthEntity entity) {
        return DataSourceHealth.builder()
            .bizId(entity.getBizId())
            .sourceCode(entity.getSourceCode())
            .lastSuccessAt(entity.getLastSuccessAt())
            .lastFailureAt(entity.getLastFailureAt())
            .successRate(entity.getSuccessRate())
            .avgLatencyMs(entity.getAvgLatencyMs())
            .failureReason(entity.getFailureReason())
            .sampleCount(entity.getSampleCount())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /** 将质量快照持久化实体转换为领域对象。 */
    private DataQualitySnapshot toDomain(DataQualitySnapshotEntity entity) {
        return DataQualitySnapshot.builder()
            .bizId(entity.getBizId())
            .sourceCode(entity.getSourceCode())
            .dataType(entity.getDataType())
            .qualityScore(entity.getQualityScore())
            .missingRate(entity.getMissingRate())
            .duplicateRate(entity.getDuplicateRate())
            .freshnessScore(entity.getFreshnessScore())
            .sampleCount(entity.getSampleCount())
            .snapshotTime(entity.getSnapshotTime())
            .detail(entity.getDetail())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
