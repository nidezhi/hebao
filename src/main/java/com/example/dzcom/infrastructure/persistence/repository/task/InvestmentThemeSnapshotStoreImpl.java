package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentThemeSnapshotEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.InvestmentThemeSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
